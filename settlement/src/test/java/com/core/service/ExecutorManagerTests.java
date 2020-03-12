package com.core.service;

import com.alibaba.fastjson.JSON;
import com.core.SettlementApplicationTests;
import com.core.constant.CouponCategory;
import com.core.constant.GoodsInfo;
import com.core.constant.GoodsType;
import com.core.constant.SettlementInfo;
import com.core.exception.CouponException;
import com.core.executor.ExecutorManager;
import com.core.vo.CouponTemplateSDK;
import com.core.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
public class ExecutorManagerTests extends SettlementApplicationTests {
    private Long fakeUserId = 20001L;

    @Autowired
    private ExecutorManager manager;

    @Test
    public void testComputeRule() throws CouponException{
        // 满减优惠劵结算测试
        log.info("满减优惠劵结算测试");
        SettlementInfo settlementInfo = fakeManJianCouponSettlement();

        SettlementInfo result = manager.computeRule(settlementInfo);
        log.info("{}",result.getCost());
        log.info("{}",result.getCouponAndTemplateInfos().size());
        log.info("{}",result.getCouponAndTemplateInfos());

        // 折扣优惠劵结算测试
        log.info("折扣优惠劵结算测试");
        SettlementInfo settlementInfo02 = fakeZheKouCouponSettlement();

        SettlementInfo result02 = manager.computeRule(settlementInfo02);
        log.info("{}",result02.getCost());
        log.info("{}",result02.getCouponAndTemplateInfos().size());
        log.info("{}",result02.getCouponAndTemplateInfos());

        // 立减优惠劵结算测试
        log.info("立减优惠劵结算测试");
        SettlementInfo settlementInfo03 = fakeLiJianCouponSettlement();

        SettlementInfo result03 = manager.computeRule(settlementInfo03);
        log.info("{}",result03.getCost());
        log.info("{}",result03.getCouponAndTemplateInfos().size());
        log.info("{}",result03.getCouponAndTemplateInfos());

        // 满减折扣优惠劵结算测试
        log.info("满减折扣优惠劵结算测试");
        SettlementInfo settlementInfo04 = fakeManJianAndZheKouCouponSettlement();

        SettlementInfo result04 = manager.computeRule(settlementInfo04);
        log.info("{}",result04.getCost());
        log.info("{}",result04.getCouponAndTemplateInfos().size());
        log.info("{}",result04.getCouponAndTemplateInfos());
    }

    private SettlementInfo fakeManJianCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);
        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(14);
        goodsInfo01.setPrice(10.0);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(5.0);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.MANJIAN.getCode());
        templateSDK.setKey("100120200801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(20, 199));
        rule.setUsage(new TemplateRule.Usage("北京省", "朝阳区",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(),
                        GoodsType.JIAJU.getCode()))));
        templateSDK.setRule(rule);

        ctInfo.setTemplate(templateSDK);

        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo fakeZheKouCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);
        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(14);
        goodsInfo01.setPrice(10.0);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(5.0);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();

        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(2);
        templateSDK.setCategory(CouponCategory.ZHEKOU.getCode());
        templateSDK.setKey("100120200228");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(85, 1));
        rule.setUsage(new TemplateRule.Usage("北京省", "朝阳区",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(),
                        GoodsType.JIAJU.getCode()))));
        templateSDK.setRule(rule);

        ctInfo.setTemplate(templateSDK);

        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo fakeLiJianCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);
        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(14);
        goodsInfo01.setPrice(10.0);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(5.0);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();

        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(2);
        templateSDK.setCategory(CouponCategory.LIJIAN.getCode());
        templateSDK.setKey("100120200226");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setUsage(new TemplateRule.Usage("北京省", "朝阳区",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(),
                        GoodsType.JIAJU.getCode()))));
        templateSDK.setRule(rule);

        ctInfo.setTemplate(templateSDK);

        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo fakeManJianAndZheKouCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);
        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(15);
        goodsInfo01.setPrice(10.0);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(5.0);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo manJian = new SettlementInfo.CouponAndTemplateInfo();
        SettlementInfo.CouponAndTemplateInfo zheKou = new SettlementInfo.CouponAndTemplateInfo();

        manJian.setId(1);
        CouponTemplateSDK manJianTemplate = new CouponTemplateSDK();
        manJianTemplate.setId(1);
        manJianTemplate.setCategory(CouponCategory.MANJIAN.getCode());
        manJianTemplate.setKey("100120200301");
        TemplateRule manJianRule = new TemplateRule();
        manJianRule.setDiscount(new TemplateRule.Discount(20, 199));
        manJianRule.setUsage(new TemplateRule.Usage("北京省", "朝阳区",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(),
                        GoodsType.JIAJU.getCode()))));
        manJianRule.setWeight(JSON.toJSONString(Collections.emptyList()));
        manJianTemplate.setRule(manJianRule);
        manJian.setTemplate(manJianTemplate);

        CouponTemplateSDK zheKouTemplate = new CouponTemplateSDK();
        zheKouTemplate.setId(1);
        zheKouTemplate.setCategory(CouponCategory.ZHEKOU.getCode());
        zheKouTemplate.setKey("100120200301");
        TemplateRule zheKouRule = new TemplateRule();
        zheKouRule.setDiscount(new TemplateRule.Discount(85, 1));
        zheKouRule.setUsage(new TemplateRule.Usage("北京省", "朝阳区",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(),
                        GoodsType.JIAJU.getCode()))));
        zheKouRule.setWeight(JSON.toJSONString(
                Collections.singleton("1001202003010001")
        ));
        zheKouTemplate.setRule(zheKouRule);
        zheKou.setTemplate(zheKouTemplate);

        info.setCouponAndTemplateInfos(Arrays.asList(manJian, zheKou));
        return info;
    }
}
