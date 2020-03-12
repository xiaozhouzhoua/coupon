package com.core.service;

import com.alibaba.fastjson.JSON;
import com.core.CoreApplicationTests;
import com.core.constant.CouponCategory;
import com.core.constant.DistributeTarget;
import com.core.constant.PeriodType;
import com.core.constant.ProductLine;
import com.core.exception.CouponException;
import com.core.vo.TemplateRequest;
import com.core.vo.TemplateRule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BuildTemplateTest extends CoreApplicationTests {
    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void buildTemplateTest() throws CouponException {
        System.out.println(JSON.toJSONString(
                buildTemplateService.buildTemplate(mockTemplateRequest())
        ));
        // 等待异步任务执行完成
        try {
            TimeUnit.MILLISECONDS.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TemplateRequest mockTemplateRequest(){
        TemplateRequest request = new TemplateRequest();
        request.setName("优惠劵模板-" + new Date().getTime());
        request.setLogo("www.iLogo.com");
        request.setDesc("这是一张优惠劵模板");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DAMAO.getCode());
        request.setCount(10);
        request.setUserId(10001L);
        request.setTarget(DistributeTarget.SINGLE.getCode());

        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),
                1,
                DateUtils.addDays(new Date(), 60).getTime()
        ));

        rule.setDiscount(new TemplateRule.Discount(
                5, 1
        ));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage("广东省",
                "珠海市", JSON.toJSONString(Arrays.asList("手机", "电器"))));

        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        request.setRule(rule);
        return request;
    }
}
