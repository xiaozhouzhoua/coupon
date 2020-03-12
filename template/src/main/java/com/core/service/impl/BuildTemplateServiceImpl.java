package com.core.service.impl;

import com.core.dao.CouponTemplateDao;
import com.core.entity.CouponTemplate;
import com.core.exception.CouponException;
import com.core.service.IAsyncService;
import com.core.service.IBuildTemplateService;
import com.core.vo.TemplateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {

    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 创建优惠劵模板
     */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        // 参数合法性校验
        if (!request.validate()) {
            throw new CouponException("请求参数异常!");
        }
        if (null != templateDao.findByName(request.getName())) {
            throw new CouponException("已存在相同的优惠劵模板!");
        }
        CouponTemplate template = requestToTemplate(request);
        template = templateDao.save(template);
        asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    /**
     * 将TemplateRequest转换为CouponTemplate
     */
    private CouponTemplate requestToTemplate(TemplateRequest request) {
        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
