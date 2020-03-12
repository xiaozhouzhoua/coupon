package com.core.api;

import com.alibaba.fastjson.JSON;
import com.core.entity.CouponTemplate;
import com.core.exception.CouponException;
import com.core.service.IBuildTemplateService;
import com.core.service.ITemplateBaseService;
import com.core.vo.CouponTemplateSDK;
import com.core.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class CouponTemplateController {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Autowired
    private ITemplateBaseService templateBaseService;

    @PostMapping("/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException {
        log.info("收到构建优惠劵模板请求：{}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    @GetMapping("/buildTemplateInfo")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id)
            throws CouponException {
        log.info("收到id为{}的优惠劵查询", id);
        return templateBaseService.buildTemplateInfo(id);
    }

    @GetMapping("/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        log.info("查找所有可用的优惠劵模板");
        return templateBaseService.findAllUsableTemplate();
    }

    @GetMapping("/sdk/infos")
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(
            @RequestParam("ids") Collection<Integer> ids
    ) {
        log.info("收到ids为{}的优惠劵查询", JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }
}
