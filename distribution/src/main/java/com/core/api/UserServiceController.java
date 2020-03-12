package com.core.api;

import com.alibaba.fastjson.JSON;
import com.core.constant.SettlementInfo;
import com.core.entity.Coupon;
import com.core.exception.CouponException;
import com.core.service.IUserService;
import com.core.vo.AcquireTemplateRequest;
import com.core.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserServiceController {

    @Autowired
    private IUserService userService;

    @GetMapping("/coupons")
    public List<Coupon> findCouponByStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status
    ) throws CouponException {
        log.info("根据用户id: {}, status: {}查找优惠劵记录",
                userId, status);
        return userService.findCouponsByStatus(userId, status);
    }

    @GetMapping("/available")
    public List<CouponTemplateSDK> findAvailableTemplate(
            @RequestParam("userId") Long userId
    ) throws CouponException{
        log.info("根据用户id: {}查找可用的优惠劵记录", userId);
        return userService.findAvailableTemplate(userId);
    }

    @PostMapping("/acquire")
    public Coupon acquireTemplate(
            @RequestBody AcquireTemplateRequest request
    ) throws CouponException{
        log.info("领取优惠劵请求： {}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

    @PostMapping("/settlement")
    public SettlementInfo settlement(
            @RequestBody SettlementInfo info
    ) throws CouponException{
        log.info("结算请求: {}", JSON.toJSONString(info));
        return userService.settlement(info);
    }
}
