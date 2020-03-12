package com.core.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * kafka相关的服务接口定义
 */
public interface IKafkaService {

    /**
     * 消费优惠劵kafka消息
     */
    void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
