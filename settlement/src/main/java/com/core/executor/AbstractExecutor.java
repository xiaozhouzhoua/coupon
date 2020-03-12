package com.core.executor;

import com.alibaba.fastjson.JSON;
import com.core.constant.GoodsInfo;
import com.core.constant.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则执行器抽象类，用来定义通用方法
 */
public abstract class AbstractExecutor {
    /**
     * 校验商品类型与优惠劵是否匹配
     * 需要注意：
     * 1、这里实现的单品类优惠劵校验，多品类需重载该方法
     * 2、商品只需要有一个优惠劵要求的商品类型去匹配就可以
     */
    protected boolean isGoodsTypeSatisfy(SettlementInfo info) {
        List<Integer> goodsType = info.getGoodsInfos()
                .stream().map(GoodsInfo::getType)
                .collect(Collectors.toList());

        List<Integer> templateGoodsType = JSON.parseObject(
                info.getCouponAndTemplateInfos()
                .get(0).getTemplate().getRule()
                .getUsage().getGoodsType(), List.class
        );

        // 存在交集即可
        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType,
                        templateGoodsType));
    }

    /**
     * 处理商品类型与优惠劵限制不匹配的情况
     * 根据用户传递的结算信息和商品总价，返回修正的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(
            SettlementInfo info, double goodsSum
    ) {
        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy(info);
        // 不符合的情况
        if (!isGoodsTypeSatisfy) {
            // 商品设置为原始总价
            info.setCost(goodsSum);
            // 设置用户的优惠劵信息为空
            info.setCouponAndTemplateInfos(Collections.emptyList());
            return info;
        }
        return null;
    }

    /**
     * 商品总价
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(
                g -> g.getPrice() * g.getCount()
        ).sum();
    }

    /**
     * 四舍五入保留两位小数
     */
    protected double retain2Decimals(double value) {
        return new BigDecimal(value)
                .setScale(2,
                        BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * 最小支付费用，避免立减劵导致费用为负数
     */
    protected double minCost() {
        return 0.1;
    }
}
