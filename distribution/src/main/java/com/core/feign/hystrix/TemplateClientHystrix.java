package com.core.feign.hystrix;

import com.core.feign.TemplateFeignClient;
import com.core.vo.CommonResponse;
import com.core.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TemplateClientHystrix implements TemplateFeignClient {
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplat() {
        log.error("客户端Template不可用");
        return new CommonResponse<>(
                -1,
                "service error",
                Collections.emptyList()
        );
    }

    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("客户端Template不可用");
        return new CommonResponse<>(
                -1,
                "service error",
                new HashMap<>()
        );
    }
}
