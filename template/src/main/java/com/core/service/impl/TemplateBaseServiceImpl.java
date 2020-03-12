package com.core.service.impl;

import com.core.dao.CouponTemplateDao;
import com.core.entity.CouponTemplate;
import com.core.exception.CouponException;
import com.core.service.ITemplateBaseService;
import com.core.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {

    @Autowired
    private CouponTemplateDao templateDao;

    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = templateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("优惠劵模板不存在: " + id);
        }
        return template.get();
    }

    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        List<CouponTemplate> templates = templateDao.findAllByAvailableAndExpired(
                true, false);
        return templates.stream()
                .map(this::templateToTemplateSDK)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);
        return templates.stream()
                .map(this::templateToTemplateSDK)
                .collect(Collectors.toMap(CouponTemplateSDK::getId,
                        Function.identity()));
    }

    /**
     * 将CouponTemplate转换为CouponTemplateSDK
     */
    private CouponTemplateSDK templateToTemplateSDK(CouponTemplate template) {
        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                template.getKey(),
                template.getTarget().getCode(),
                template.getRule()
        );
    }
}
