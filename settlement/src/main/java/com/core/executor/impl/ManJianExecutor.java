package com.core.executor.impl;

import com.core.constant.RuleFlag;
import com.core.constant.SettlementInfo;
import com.core.executor.AbstractExecutor;
import com.core.executor.RuleExecutor;
import com.core.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
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
            log.debug("满减优惠劵不满足商品类型！");
            return probability;
        }
        // 判断优惠劵是否符合折扣标准
        CouponTemplateSDK templateSDK = info.getCouponAndTemplateInfos()
                .get(0).getTemplate();

        double base = (double)templateSDK.getRule().getDiscount().getBase();
        // 额度
        double quota = templateSDK.getRule().getDiscount().getQuota();

        // 如果不符合标准则直接返回商品总价
        if(goodsSum < base) {
            log.debug("当前优惠劵不满足消费基准");
            info.setCost(goodsSum);
            info.setCouponAndTemplateInfos(Collections.emptyList());
            return info;
        }

        // 计算使用优惠劵之后的价格
        info.setCost(retain2Decimals(
                Math.max((goodsSum - quota), minCost()))
        );
        log.debug("使用满减优惠劵，原始费用：{}, 实际费用：{}",
                goodsSum, info.getCost());
        return info;
    }
}
