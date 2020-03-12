package com.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {
    /**
     * 优惠劵状态
     */
    private Integer status;

    /**
     * 优惠劵主键
     */
    private List<Integer> ids;
}
