package com.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 有效期类型
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum PeriodType {

    REGULAR("固定-固定日期", 1),
    SHIFT("变动-从领取时间计算", 2);

    private String description;

    private Integer code;

    public static PeriodType of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + "not exists!"));
    }
}
