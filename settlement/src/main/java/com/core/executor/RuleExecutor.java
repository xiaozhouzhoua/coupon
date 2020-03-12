package com.core.executor;

import com.core.constant.RuleFlag;
import com.core.constant.SettlementInfo;

/**
 * 优惠劵模板规则处理器接口定义
 */
public interface RuleExecutor {
    /**
     * 规则类型标记
     */
    RuleFlag ruleConfig();

    /**
     * 优惠劵规则的计算
     * 返回修正后的结算信息
     */
    SettlementInfo computeRule(SettlementInfo info);
}
