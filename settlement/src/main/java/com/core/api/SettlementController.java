package com.core.api;

import com.alibaba.fastjson.JSON;
import com.core.constant.SettlementInfo;
import com.core.exception.CouponException;
import com.core.executor.ExecutorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@SuppressWarnings("all")
public class SettlementController {

    @Autowired
    private ExecutorManager executorManager;

    @PostMapping("/compute")
    public SettlementInfo computeRule(
            @RequestBody SettlementInfo info
    ) throws CouponException{
        log.info("结算请求：{}", JSON.toJSONString(info));
        return executorManager.computeRule(info);
    }
}
