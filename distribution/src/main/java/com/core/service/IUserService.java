package com.core.service;

import com.core.constant.SettlementInfo;
import com.core.entity.Coupon;
import com.core.exception.CouponException;
import com.core.vo.AcquireTemplateRequest;
import com.core.vo.CouponTemplateSDK;

import java.util.List;

/**
 * 用户服务相关的接口定义
 * 1、用户三类状态优惠劵信息展示服务
 * 2、查看用户当前可以领取的优惠劵模板
 * 3、用户领取优惠劵服务
 * 4、用户消费优惠劵服务
 */
public interface IUserService {
    /**
     * 根据用户id和状态查询优惠劵信息
     */
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;

    /**
     * 根据用户id查找当前可以领取的优惠劵模板
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠劵
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * 结算优惠劵
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
