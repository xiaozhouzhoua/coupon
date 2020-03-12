package com.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.core.constant.CommonConstant;
import com.core.constant.CouponStatus;
import com.core.constant.GoodsInfo;
import com.core.constant.SettlementInfo;
import com.core.dao.CouponDao;
import com.core.entity.Coupon;
import com.core.exception.CouponException;
import com.core.feign.SettlementFeignClient;
import com.core.feign.TemplateFeignClient;
import com.core.service.IRedisService;
import com.core.service.IUserService;
import com.core.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 所有的操作过程，状态都保存在Redis中，并通过Kafka传递到Mysql
 * 为什么使用Kafka，而不是直接使用SpringBoot中的异步处理？
 * => 因为异步任务可能会失败，即使在kafka中消费消息失败，仍然可以
 * 重新从kafka中获取消息去回退记录，保证缓存中与存储中数据一致性
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private IRedisService redisService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private TemplateFeignClient templateFeignClient;

    @Resource
    private SettlementFeignClient settlementFeignClient;

    /**
     * 根据优惠劵状态查找优惠劵
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        List<Coupon> cachedCoupon = redisService.getCachedCoupon(userId, status);
        List<Coupon> preTarget;
        if (CollectionUtils.isNotEmpty(cachedCoupon)) {
            log.debug("优惠劵缓存不为空, userId: {}, 状态：{}", userId, status);
            preTarget = cachedCoupon;
        } else {
            log.debug("优惠劵缓存为空，开始从数据库中获取，{}, {}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(
                    userId, CouponStatus.of(status)
            );
            // 如果数据库中没有记录，直接返回就可以了，因为缓存中已经保存了一张无效的优惠劵
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("当前用户在数据库中没有对应的优惠劵，{}, {}", userId, status);
                return dbCoupons;
            }
            // 数据库中存在则填充dbCoupons中的templateSDK
            Map<Integer, CouponTemplateSDK> ids2TemplateSDK = templateFeignClient.findIds2TemplateSDK(dbCoupons.stream()
                    .map(Coupon::getTemplateId).collect(Collectors.toList())).getData();
            dbCoupons.forEach(dc -> dc.setTemplateSDK(ids2TemplateSDK.get(dc.getTemplateId())));
            // 数据库中存在记录
            preTarget = dbCoupons;
            // 写入缓存
            redisService.addCouponToCache(userId, preTarget, status);
        }
        // 将无效优惠劵剔除
        preTarget = preTarget.stream()
                .filter(c -> c.getId() != -1)
                .collect(Collectors.toList());
        // 如果当前获取的是可用优惠劵，还需要做对过期优惠劵的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            // 如果已过期状态不为空，需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("添加已过期优惠劵到缓存, {}, {}", userId, status);
                redisService.addCouponToCache(userId,
                        classify.getExpired(), CouponStatus.EXPIRED.getCode());

                // 发送到kafka中做异步处理
                kafkaTemplate.send(
                        CommonConstant.TOPIC,
                        JSON.toJSONString(new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                classify.getExpired().stream()
                                        .map(Coupon::getId)
                                        .collect(Collectors.toList())
                        ))
                );
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    /**
     * 根据用户id查找当前可以领取的优惠劵模板
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = System.currentTimeMillis();
        List<CouponTemplateSDK> templateSDKS = templateFeignClient
                .findAllUsableTemplat().getData();
        log.debug("获取的优惠劵模板数量：{}", templateSDKS.size());
        // 过滤过期的优惠劵模板，定时任务延迟需要确认
        templateSDKS = templateSDKS.stream().filter(
                t -> t.getRule().getExpiration().getDeadline() > curTime)
                .collect(Collectors.toList());
        log.info("可用的优惠劵模板数量：{}", templateSDKS.size());
        // key是TemplateId，value中的left是Template的limitation字段，right是优惠劵模板本身
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template =
                new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(t -> limit2Template.put(t.getId(),
                Pair.of(t.getRule().getLimitation(), t)));
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId,
                CouponStatus.USABLE.getCode());
        log.debug("当前用户：{}拥有的可用优惠劵数量：{}", userId, userUsableCoupons.size());
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        // 根据Template的rule判断是否可以领取优惠劵模板
        limit2Template.forEach((k, v) -> {
            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            // 优惠劵模板领取达到上限
            if (templateId2Coupons.containsKey(k)
                    && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * 用户领取优惠劵
     * 1、从TemplateFeignClient拿到优惠劵并检查是否过期
     * 2、根据limitation判断用户是否可以领取
     * 3、保存到数据库
     * 4、填充CouponTemplateSDK
     * 5、保存到缓存中
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateFeignClient.findIds2TemplateSDK(
                        Collections.singletonList(request.getTemplateSDK().getId())).getData();
        // 优惠劵模板是否存在
        if (id2TemplateSDK.size() <= 0) {
            log.error("无法根据模板id：{}领取优惠劵", request.getTemplateSDK().getId());
            throw new CouponException("优惠劵模板不存在，无法领取优惠劵");
        }
        // 用户是否可以领取这张优惠劵
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                request.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
                request.getTemplateSDK().getRule().getLimitation()) {
            log.error("用户可以领取的优惠劵模板超出上限");
            throw new CouponException("用户可以领取的优惠劵模板超出上限");
        }
        // 尝试去获取优惠劵码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        if (StringUtils.isEmpty(couponCode)) {
            log.error("无法领取优惠劵码：{}, 优惠劵码已发放完", couponCode);
            throw new CouponException("优惠劵码已发放完");
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(),
                request.getUserId(),
                couponCode,
                CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);
        // 开始填充CouponTemplateSDK，一定要在缓存前操作
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        // 放入缓存中
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );
        return newCoupon;
    }

    /**
     * 结算优惠劵
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        // 当没有传递优惠劵时，直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos =
                info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)) {
            log.info("结算空的优惠劵");
            double goodsSum = 0.0;
            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() * gi.getCount();
            }
            // 没有优惠劵也就不存在优惠劵的核销
            info.setCost(retain2Decimals(goodsSum));
        }
        // 校验传递的优惠劵是否是用户自己的
        List<Coupon> coupons = findCouponsByStatus(
                info.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, Coupon> id2Coupon = coupons.stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));
        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId)
                        .collect(Collectors.toList()), id2Coupon.keySet()
        )) {
            log.error("用户优惠劵不可用，不属于用户自己的优惠劵");
            throw new CouponException("用户优惠劵不可用，不属于用户自己的优惠劵");
        }
        log.debug("用户当前优惠劵可用数量：{}", ctInfos.size());
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci ->
            settleCoupons.add(id2Coupon.get(ci.getId())));
        // 通过结算服务获取结算信息
        SettlementInfo processedInfo = settlementFeignClient
                .computeRule(info).getData();
        if (processedInfo.getEmploy()
                && CollectionUtils.isNotEmpty(processedInfo.getCouponAndTemplateInfos())) {
            log.info("用户信息：{}, 结算信息：{}", info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            // 更新缓存
            redisService.addCouponToCache(info.getUserId(),
                    settleCoupons, CouponStatus.USED.getCode());
            // kafka更新db
            kafkaTemplate.send(
                    CommonConstant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream()
                                    .map(Coupon::getId)
                                    .collect(Collectors.toList())
                    ))
            );
        }
        return processedInfo;
    }

    /**
     * 保留两位小数
     */
    private double retain2Decimals(double value) {
        // BigDecimal.ROUND_HALF_UP表示四舍五入
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
