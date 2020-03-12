package com.core.service;


import com.core.entity.CouponTemplate;
import com.core.exception.CouponException;
import com.core.vo.TemplateRequest;

/**
 * 构建优惠劵模板接口定义
 */
public interface IBuildTemplateService {
    /**
     * 创建优惠劵模板
     * @param request {@link TemplateRequest}
     * @return {@link CouponTemplate}
     */
    CouponTemplate buildTemplate(TemplateRequest request) throws CouponException;
}
