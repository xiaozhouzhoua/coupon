package com.core.feign;

import com.core.constant.SettlementInfo;
import com.core.exception.CouponException;
import com.core.feign.hystrix.SettlementClientHystrix;
import com.core.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "coupon-settlement",
        fallback = SettlementClientHystrix.class)
public interface SettlementFeignClient {
    @RequestMapping(value = "/settlement/compute", method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(
            @RequestBody SettlementInfo settlement
    ) throws CouponException;
}
