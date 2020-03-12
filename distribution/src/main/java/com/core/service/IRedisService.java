package com.core.service;

import com.core.entity.Coupon;
import com.core.exception.CouponException;

import java.util.List;

/**
 * redis相关的操作服务接口定义
 * 1、用户的三个状态优惠劵Cache操作
 * 2、优惠劵模板生成的优惠劵码Cache操作
 */
@SuppressWarnings("all")
public interface IRedisService {

    /**
     * 根据userId和状态找到缓存的优惠劵列表数据
     */
    List<Coupon> getCachedCoupon(Long userId, Integer status);


    /**
     * 保存空的优惠劵列表到缓存中，避免缓存穿透
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);

    /**
     * 尝试从Cache中获取一个优惠劵码
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     * 将优惠劵保存到Cache中
     */
    Integer addCouponToCache(Long userId, List<Coupon> coupons,
                             Integer status) throws CouponException;
}
