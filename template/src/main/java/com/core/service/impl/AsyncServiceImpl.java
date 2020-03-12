package com.core.service.impl;

import com.core.constant.CommonConstant;
import com.core.dao.CouponTemplateDao;
import com.core.entity.CouponTemplate;
import com.core.service.IAsyncService;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private CouponTemplateDao templateDao;

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 根据模板异步地创建优惠劵码
     */
    @Async("getAsyncExecutor")
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> couponCode = buildCouponCode(template);

        String redisKey = String.format("%s%s",
                CommonConstant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());
        log.info("发送优惠劵码到Redis: {}",
                redisTemplate.opsForList().rightPushAll(redisKey, couponCode));

        template.setAvailable(true);
        templateDao.save(template);

        watch.stop();
        log.info("根据优惠劵模板生成优惠劵码耗时：{}ms", watch.elapsed(TimeUnit.MILLISECONDS));
        log.info("优惠劵{}已经可用！", template.getId());
    }

    /**
     * 构建优惠劵码 - 18位
     */
    private Set<String> buildCouponCode(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> result = new HashSet<>(template.getCount());
        String prefix4 = template.getProductLine().getCode().toString()
                + template.getCategory().getCode();

        String date = new SimpleDateFormat("yyMMdd")
                .format(template.getCreateTime());

        for (int i = 0; i != template.getCount(); ++i) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        // 对可能重复的优惠劵码进行再次构建生成，避免少生成
        while (result.size() < template.getCount()) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        assert result.size() == template.getCount();

        watch.stop();
        log.info("生成优惠劵耗时：{}ms", watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    /**
     * 构建优惠劵码的后14位
     * @param date 创建优惠劵的日期
     * @return 14位的优惠劵码
     */
    private String buildCouponCodeSuffix14(String date) {
        char[] bases = new char[]{'1', '2' ,'3', '4', '5', '6', '7', '8', '9'};
        List<Character> chars = date.chars()
                .mapToObj(e -> (char)e)
                .collect(Collectors.toList());
        // 洗牌算法
        Collections.shuffle(chars);
        String mid6 = chars.stream()
                .map(Object::toString)
                .collect(Collectors.joining());

        String suffix8 = RandomStringUtils.random(1, bases)
                + RandomStringUtils.randomNumeric(7);

        return mid6 + suffix8;
    }
}
