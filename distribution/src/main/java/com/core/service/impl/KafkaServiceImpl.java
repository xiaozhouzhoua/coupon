package com.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.core.constant.CommonConstant;
import com.core.constant.CouponStatus;
import com.core.dao.CouponDao;
import com.core.entity.Coupon;
import com.core.service.IKafkaService;
import com.core.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 核心思想：将缓存中优惠劵的变化保存到数据库中
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class KafkaServiceImpl implements IKafkaService {

    @Autowired
    private CouponDao couponDao;

    @Override
    @KafkaListener(topics = {CommonConstant.TOPIC}, groupId = "coupon-1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(message.toString(),
                    CouponKafkaMessage.class);
            log.info("收到kafka消息：{}", message.toString());
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());
            switch (status) {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupon(couponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupon(couponInfo, status);
                    break;
            }
        }
    }

    private void processUsedCoupon(CouponKafkaMessage kafkaMessage,
                                   CouponStatus status) {
        processCouponByStatus(kafkaMessage, status);
    }

    private void processExpiredCoupon(CouponKafkaMessage kafkaMessage,
                                      CouponStatus status) {
        processCouponByStatus(kafkaMessage, status);
    }

    /**
     * 根据状态处理优惠劵
     */
    private void processCouponByStatus(CouponKafkaMessage kafkaMessage,
                                       CouponStatus status) {
        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());
        if (CollectionUtils.isEmpty(coupons)
                || coupons.size() != kafkaMessage.getIds().size()) {
            log.error("找不到正确的优惠劵信息: {}", JSON.toJSONString(kafkaMessage));
            return;
        }
        coupons.forEach(coupon -> coupon.setStatus(status));
        log.info("kafka处理的优惠劵数量：{}",
                couponDao.saveAll(coupons).size());
    }
}
