package com.core.constant;

/**
 * 通用常量定义
 */
public class CommonConstant {
    /**
     * kafka消息的topic
     */
    public static final String TOPIC = "user_coupon_op";

    /**
     * Redis key前缀定义
     */
    public static class RedisPrefix {
        /**
         * 优惠劵码key前缀
         */
        public static final String COUPON_TEMPLATE = "coupon_template_code_";
        /**
         * 用户当前所有可用的优惠劵key前缀
         */
        public static final String USER_COUPON_USABLE = "user_coupon_usable_";

        /**
         * 用户当前已使用的优惠劵key前缀
         */
        public static final String USER_COUPON_USED = "user_coupon_used_";
        /**
         * 用户当前已过期的优惠劵key前缀
         */
        public static final String USER_COUPON_EXPIRED = "user_coupon_expired_";
    }
}
