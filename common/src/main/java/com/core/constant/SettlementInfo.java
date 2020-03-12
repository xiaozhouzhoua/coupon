package com.core.constant;

import com.core.vo.CouponTemplateSDK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结算信息对象定义
 * 包含：
 * 1、userId
 * 2、商品信息列表
 * 3、优惠劵列表
 * 4、结算结果金额
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品信息
     */
    private List<GoodsInfo> goodsInfos;

    /**
     * 优惠劵列表
     */
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    /**
     * 是否使结算生效，即核销还是结算
     */
    private Boolean employ;

    /**
     * 结算金额
     */
    private Double cost;
    /**
     * 优惠劵和模板信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponAndTemplateInfo {
        /**
         * Coupon的主键
         */
        private Integer id;

        private CouponTemplateSDK template;
    }
}

