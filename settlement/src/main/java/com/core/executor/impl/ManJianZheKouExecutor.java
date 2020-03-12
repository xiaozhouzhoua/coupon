package com.core.executor.impl;

import com.alibaba.fastjson.JSON;
import com.core.constant.CouponCategory;
import com.core.constant.GoodsInfo;
import com.core.constant.RuleFlag;
import com.core.constant.SettlementInfo;
import com.core.executor.AbstractExecutor;
import com.core.executor.RuleExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("all")
public class ManJianZheKouExecutor extends AbstractExecutor
        implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     * 重载单品类商品类型校验作为多品类类型校验
     * 满减+折扣优惠劵的校验
     */
    @Override
    protected boolean isGoodsTypeSatisfy(SettlementInfo info) {
        log.debug("检查满减折扣优惠劵是否和商品类型匹配");
        List<Integer> goodsType = info.getGoodsInfos()
                .stream().map(GoodsInfo::getType)
                .collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        info.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(JSON.parseObject(
                    ct.getTemplate().getRule().getUsage().getGoodsType(),
                    List.class
            ));
        });
        // 如果想要使用多类优惠劵，则需要所有的商品类型都包含在内，即差集为空
        return CollectionUtils.isEmpty(CollectionUtils.subtract(
                goodsType,templateGoodsType
        ));
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
            log.debug("满减折扣优惠劵不满足商品类型！");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zheKou = null;
        for (SettlementInfo.CouponAndTemplateInfo ct :
                info.getCouponAndTemplateInfos()) {
            if (CouponCategory.of(ct.getTemplate().getCategory())
                    == CouponCategory.MANJIAN) {
                manJian = ct;
            } else {
                zheKou = ct;
            }
        }
        assert null != manJian;
        assert null != zheKou;

        // 当前的优惠劵和满减优惠劵如果不能共用，则清空优惠劵，返回原价
        if (!isTemplateCanShared(manJian, zheKou)) {
            log.debug("当前的满减折扣优惠劵不能共用");
            info.setCost(goodsSum);
            info.setCouponAndTemplateInfos(Collections.emptyList());
            return info;
        }
        // 开始进行结算
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manJianBase = (double) manJian.getTemplate()
                .getRule().getDiscount().getBase();

        double manJianQuota = (double) manJian.getTemplate()
                .getRule().getDiscount().getQuota();

        double zheKouQuota = (double) zheKou.getTemplate()
                .getRule().getDiscount().getQuota();

        // 最终计算价格
        double targetSum = goodsSum;
        if (targetSum >= manJianBase) {
            targetSum -= manJianQuota;
            ctInfos.add(manJian);
        }

        targetSum *= zheKouQuota * 1.0 / 100;
        ctInfos.add(zheKou);
        // 填充实际使用的优惠劵信息
        info.setCouponAndTemplateInfos(ctInfos);
        // 设置总价
        info.setCost(retain2Decimals(Math.max(targetSum, minCost())));
        log.debug("使用满减折扣优惠劵，原始费用：{}，实际费用：{}",
                goodsSum, info.getCost());
        return info;
    }

    /**
     * 当前两张优惠劵是否可以共用，即校验
     * TemplateRule中的weight是否满足条件
     */
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                                        SettlementInfo.CouponAndTemplateInfo zheKou) {
        String manJianKey = manJian.getTemplate().getKey()
                + String.format("%04d", manJian.getTemplate().getId());

        String zheKouKey = zheKou.getTemplate().getKey()
                + String.format("%04d", zheKou.getTemplate().getId());

        List<String> allSharedKeysForManJian = new ArrayList<>();
        // 首先包含自身
        allSharedKeysForManJian.add(manJianKey);
        // 其次包含weight中定义的优惠劵
        allSharedKeysForManJian.addAll(JSON.parseObject(
                manJian.getTemplate().getRule().getWeight(),
                List.class)
        );

        List<String> allSharedKeysForZheKou = new ArrayList<>();
        allSharedKeysForZheKou.add(zheKouKey);

        allSharedKeysForZheKou.addAll(JSON.parseObject(
                zheKou.getTemplate().getRule().getWeight(),
                List.class)
        );

        return CollectionUtils.isSubCollection(
                Arrays.asList(manJianKey, zheKouKey), allSharedKeysForManJian)
                || CollectionUtils.isSubCollection(
                Arrays.asList(manJianKey, zheKouKey), allSharedKeysForZheKou);
    }
}
