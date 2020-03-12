package com.core.executor;

import com.core.constant.CouponCategory;
import com.core.constant.RuleFlag;
import com.core.constant.SettlementInfo;
import com.core.exception.CouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠劵结算规则执行管理器
 * 根据用户的请求(SettlementInfo)
 * 找到对应的执行器(Executor) 去做结算
 * BeanPostProcessor: spring bean的后置处理器
 * 即spring中所有bean加载完成才初始化这个bean
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class ExecutorManager implements BeanPostProcessor {
    // 规则执行器映射
    private static Map<RuleFlag, RuleExecutor> executorIndex =
            new HashMap<>(RuleFlag.values().length);

    /**
     * 优惠劵结算规则计算入口
     * 注意：一定要保证传递进来的优惠劵个数大于等于一
     */
    public SettlementInfo computeRule(SettlementInfo info)
            throws CouponException {
        SettlementInfo result = null;
        // 单类优惠劵
        if (info.getCouponAndTemplateInfos().size() == 1) {
            CouponCategory category = CouponCategory.of(info.getCouponAndTemplateInfos()
                    .get(0).getTemplate().getCategory());

            switch (category) {
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(info);
                    break;
                case ZHEKOU:
                    result = executorIndex.get(RuleFlag.ZHEKOU).computeRule(info);
                    break;
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN).computeRule(info);
                    break;
            }
        } else {
            // 多类优惠劵
            List<CouponCategory> categories = new ArrayList<>(
                    info.getCouponAndTemplateInfos().size());

            info.getCouponAndTemplateInfos().forEach(ct ->
                    categories.add(CouponCategory.of(ct.getTemplate().getCategory())));

            if (categories.size() != 2) {
                throw new CouponException("不支持更多的优惠劵种类");
            } else {
                if (categories.contains(CouponCategory.MANJIAN)
                        && categories.contains(CouponCategory.ZHEKOU)) {
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(info);
                } else {
                    throw new CouponException("不支持更多的优惠劵种类");
                }
            }
        }
        return result;
    }

    /**
     * 在该bean初始化之前去执行
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if (!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if (executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("重复的规则执行器");
        }

        log.info("为{}优惠劵，加载执行器：{}", ruleFlag, executor.getClass());
        executorIndex.put(ruleFlag, executor);
        return null;
    }

    /**
     * 在该bean初始化之后去执行
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
