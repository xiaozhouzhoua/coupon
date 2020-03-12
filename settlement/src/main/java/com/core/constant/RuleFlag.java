package com.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则类型枚举定义
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum RuleFlag {
    // 单类别优惠劵定义
    MANJIAN("满减劵的计算规则"),
    ZHEKOU("折扣劵的计算规则"),
    LIJIAN("立减劵的计算规则"),

    // 多类别优惠劵定义
    MANJIAN_ZHEKOU("满减劵+折扣劵的计算规则");

    private String description;
}
