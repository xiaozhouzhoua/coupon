package com.core.dao;

import com.core.constant.CouponStatus;
import com.core.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 优惠劵实体类操作
 */
public interface CouponDao extends JpaRepository<Coupon, Integer> {
    /**
     * 根据userId + 状态查找优惠劵
     */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
