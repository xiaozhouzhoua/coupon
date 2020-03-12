package com.core.service;

import com.alibaba.fastjson.JSON;
import com.core.CoreApplicationTests;
import com.core.exception.CouponException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class TemplateBaseTest extends CoreApplicationTests {

    @Autowired
    private ITemplateBaseService templateBaseService;

    @Test
    public void testBuildTemplateInfo() throws CouponException {
        System.out.println(JSON.toJSONString(templateBaseService.buildTemplateInfo(1)));
    }

    @Test
    public void testFindAllUsableTemplate() {
        System.out.println(JSON.toJSONString(templateBaseService.findAllUsableTemplate()));
    }

    @Test
    public void testFindIds2TemplateSDK() {
        System.out.println(JSON.toJSONString(templateBaseService.findIds2TemplateSDK(Arrays.asList(1,2,3))));
    }
}
