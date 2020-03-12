package com.core.service;

import com.core.entity.CouponTemplate;
import com.core.exception.CouponException;
import com.core.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠劵模板基础服务定义
 */
public interface ITemplateBaseService {
    /**
     * 根据优惠劵模板ID获取优惠劵模板信息
     */
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

    /**
     * 查找所有可用的优惠劵模板
     */
    List<CouponTemplateSDK> findAllUsableTemplate();

    /**
     * 获取模板ids到CouponTemplateSDK的映射
     */
    Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
