package com.core.vo;

import com.core.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 优惠劵规则对象定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class TemplateRule {

    private Expiration expiration;

    private Discount discount;

    /**
     * 每个人最多可以领取几张
     */
    private Integer limitation;

    private Usage usage;

    /**
     * 权重（可以和哪些优惠劵叠加使用，同一类型优惠劵一定不能叠加）
     */
    private String weight;

    public boolean validate() {
        return expiration.validate() && discount.validate()
                && limitation > 0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    /**
     * 有效期限规则
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Expiration{
        /**
         * 有效期规则，对应PeriodType的code字段
         */
        private Integer period;

        /**
         * 有效间隔：只对变动性有效期有效
         */
        private Integer gap;

        /**
         * 优惠劵模板的失效日期，两类规则都有效
         */
        private Long deadline;

        boolean validate() {
            return null != PeriodType.of(period)
                    && gap > 0 && deadline > 0;
        }
    }

    /**
     * 折扣，需要与类型配合决定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Discount {
        /**
         * 额度，满减（20），折扣（85），立减（10）
         */
        private Integer quota;

        /**
         * 基准：需要满多少才可用
         */
        private Integer base;

        boolean validate() {
            return quota > 0 && base > 0;
        }
    }

    /**
     * 使用范围
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 商品类型：文娱、生鲜、电器。。。
         */
        private String goodsType;

        boolean validate() {
            return StringUtils.isNotEmpty(province)
                    && StringUtils.isNotEmpty(city)
                    && StringUtils.isNotEmpty(goodsType);
        }
    }
}
