package com.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微服务之间用的优惠劵模板信息定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateSDK {
    /**
     * 优惠劵模板主键
     */
    private Integer id;

    /**
     * 优惠劵模板名称
     */
    private String name;

    /**
     * 优惠劵logo
     */
    private String logo;

    /**
     * 优惠劵描述
     */
    private String desc;

    /**
     * 优惠劵分类
     */
    private String category;

    /**
     * 产品线
     */
    private Integer productLine;

    /**
     * 优惠劵模板编码
     */
    private String key;

    /**
     * 目标用户
     */
    private Integer target;

    /**
     * 优惠劵规则
     */
    private TemplateRule rule;
}
