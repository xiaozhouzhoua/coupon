package com.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.core.constant.CommonConstant;
import com.core.constant.CouponStatus;
import com.core.entity.Coupon;
import com.core.exception.CouponException;
import com.core.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis相关操作服务接口实现
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class RedisServiceImpl implements IRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<Coupon> getCachedCoupon(Long userId, Integer status) {
        log.info("从缓存中获取优惠劵信息, 用户：{}, 状态：{}", userId, status);
        String redisKey = status2RedisKey(status, userId);
        List<String> couponStrings = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrings)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrings.stream()
                .map(cs -> JSON.parseObject(cs, Coupon.class))
                .collect(Collectors.toList());
    }

    /**
     * 塞入空的coupon避免缓存穿透
     * 缓存对象内容
     * k: status -> redisKey
     * v: {coupon_id, 序列化的对象}
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("保存空数据到缓存到用户: {}, 状态: {}", userId, JSON.toJSONString(status));
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));
        // 使用SessionCallback把数据命令放入到redis的pipeline
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("all")
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);
                    redisOperations.opsForHash().putAll(redisKey, invalidCouponMap);
                });
                return null;
            }
        };
        log.info("Redis Pipeline执行结果: {}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s",
                CommonConstant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("从缓存中获取优惠劵码, templateId：{}, redisKey：{}, couponCode：{}",
                templateId, redisKey, couponCode);
        return couponCode;
    }

    @Override
    public Integer addCouponToCache(Long userId,
                                    List<Coupon> coupons,
                                    Integer status) throws CouponException {
        log.info("添加优惠劵信息到缓存，userId：{}, coupons：{}, status：{}",
                userId, JSON.toJSONString(coupons), status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }
        return result;
    }

    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {
        log.debug("为可用优惠劵添加缓存");
        Map<String, String> needCachedForUsable = new HashMap<>(coupons.size());
        coupons.forEach(c -> needCachedForUsable.put(c.getId().toString(),
                JSON.toJSONString(c)));
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, needCachedForUsable);
        log.info("为可用优惠劵环境添加了{}个缓存， userId：{}, redisKey：{}",
                needCachedForUsable.size(), userId, redisKey);
        // 添加随机过期时间
        redisTemplate.expire(redisKey, getRandomExpirationTime(2, 3), TimeUnit.SECONDS);
        return needCachedForUsable.size();
    }

    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException {
        // 如果status是used，代表用户操作是使用当前的优惠劵，将影响到两个Cache(USABLE、USED)
        log.debug("为已使用的优惠劵添加缓存");
        Map<String, String> needCachedForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);
        // 获取当前用户可用的优惠劵
        List<Coupon> curUsableCoupons = getCachedCoupon(userId,CouponStatus.USABLE.getCode());
        // 当前可用的优惠劵个数一定是大于1(至少包含一个空的)
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCachedForUsed.put(c.getId().toString(),
                JSON.toJSONString(c)));

        // 校验当前的优惠劵参数是否与Cached中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("当前传递的优惠劵信息和缓存中的不匹配, userId: {}, paramIds: {}, curUsableIds: {}",
                    userId, JSON.toJSONString(paramIds), JSON.toJSONString(curUsableIds));
            throw new CouponException("当前传递的优惠劵信息和缓存中的不匹配");
        }

        List<String> needClearKeys = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 添加已使用的优惠劵缓存
                redisOperations.opsForHash().putAll(redisKeyForUsed, needCachedForUsed);
                // 清理可使用的优惠劵缓存
                redisOperations.opsForHash().delete(redisKeyForUsable, needClearKeys.toArray());
                // 重置缓存过期时间
                redisOperations.expire(redisKeyForUsable,
                        getRandomExpirationTime(2, 3), TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForUsed,
                        getRandomExpirationTime(2, 3), TimeUnit.SECONDS);

                return null;
            }
        };
        log.info("Pipeline执行结果：{}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons) throws CouponException {
        // status是expired，代表已有优惠劵过期了，usable->expired
        log.debug("为已过期的优惠劵添加缓存");
        Map<String, String> needCachedForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);

        // 从缓存中获取当前用户可用的优惠劵
        List<Coupon> curUsableCoupons = getCachedCoupon(userId, CouponStatus.USABLE.getCode());
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCachedForExpired.put(c.getId().toString(), JSON.toJSONString(c)));

        // 校验当前的优惠劵参数是否与Cached中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("当前传递的优惠劵信息和缓存中的不匹配, userId: {}, paramIds: {}, curUsableIds: {}",
                    userId, JSON.toJSONString(paramIds), JSON.toJSONString(curUsableIds));
            throw new CouponException("当前传递的优惠劵信息和缓存中的不匹配");
        }
        List<String> needClearKeys = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 添加已过期的优惠劵缓存
                redisOperations.opsForHash().putAll(redisKeyForExpired, needCachedForExpired);
                // 清理可使用的优惠劵缓存
                redisOperations.opsForHash().delete(redisKeyForUsable, needClearKeys.toArray());
                // 重置缓存过期时间
                redisOperations.expire(redisKeyForUsable,
                        getRandomExpirationTime(2, 3), TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForExpired,
                        getRandomExpirationTime(2, 3), TimeUnit.SECONDS);

                return null;
            }
        };
        log.info("Pipeline执行结果：{}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();

    }

    /**
     * 根据status获取到对应的Redis key
     */
    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s",
                        CommonConstant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s",
                        CommonConstant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s",
                        CommonConstant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    /**
     * 获取一个随机的过期时间
     * min 最小的小时数
     * max 最大的小时数
     * 返回[min, max]之间的随机秒数
     * 作用：避免缓存雪崩，即key在同一时间失效
     */
    private Long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }
}
