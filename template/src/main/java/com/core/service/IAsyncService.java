package com.core.service;

import com.core.entity.CouponTemplate;

/**
 * 异步服务接口定义
 */
@SuppressWarnings("all")
public interface IAsyncService {
    /**
     * 根据模板异步地创建优惠劵码
     */
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
