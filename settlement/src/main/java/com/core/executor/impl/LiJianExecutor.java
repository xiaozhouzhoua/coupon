package com.core.executor.impl;

import com.core.constant.RuleFlag;
import com.core.constant.SettlementInfo;
import com.core.executor.AbstractExecutor;
import com.core.executor.RuleExecutor;
import com.core.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo info) {
        double goodsSum = retain2Decimals(
                goodsCostSum(info.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                info, goodsSum
        );
        if (null != probability) {
            // 不满足条件
            log.debug("立减优惠劵不满足商品类型！");
            return probability;
        }

        // 立减优惠劵可以直接使用，没有门槛，即base值
        CouponTemplateSDK templateSDK = info.getCouponAndTemplateInfos()
                .get(0).getTemplate();

        // 额度
        double quota = templateSDK.getRule().getDiscount().getQuota();

        // 计算使用优惠劵之后的价格
        info.setCost(retain2Decimals(
                Math.max((goodsSum - quota), minCost()))
        );
        log.debug("使用立减优惠劵，原始费用：{}, 实际费用：{}",
                goodsSum, info.getCost());
        return info;
    }
}
