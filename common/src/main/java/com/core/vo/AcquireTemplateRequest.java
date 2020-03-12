package com.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取优惠劵请求对象定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcquireTemplateRequest {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 优惠劵模板信息
     */
    private CouponTemplateSDK templateSDK;
}
