package com.core.feign.hystrix;

import com.core.constant.SettlementInfo;
import com.core.exception.CouponException;
import com.core.feign.SettlementFeignClient;
import com.core.vo.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementClientHystrix implements SettlementFeignClient {
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlement) throws CouponException {
        log.error("客户端Settlement不可用");
        settlement.setEmploy(false);
        settlement.setCost(-1.0);
        return new CommonResponse<>(
                -1,
                "service error",
                settlement
        );
    }
}
