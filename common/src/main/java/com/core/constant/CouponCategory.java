package com.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 优惠劵分类
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum CouponCategory {

    MANJIAN("满减劵", "001"),
    ZHEKOU("折扣劵","002"),
    LIJIAN("立减劵", "003");

    private String description;

    private String code;

    public static CouponCategory of(String code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + "not exists!"));
    }
}
