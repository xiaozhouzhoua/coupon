package com.core.vo;

import com.core.constant.CouponStatus;
import com.core.constant.PeriodType;
import com.core.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {
    private List<Coupon> usable;
    private List<Coupon> used;
    private List<Coupon> expired;

    /**
     * 对当前的优惠劵进行分类
     */
    public static CouponClassify classify(List<Coupon> coupons) {
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(coupon -> {
            boolean isTimeExpired;
            long curTime = System.currentTimeMillis();
            if (coupon.getTemplateSDK().getRule().getExpiration().getPeriod().equals(
                    PeriodType.REGULAR.getCode()
            )) {
                isTimeExpired = coupon.getTemplateSDK().getRule().getExpiration()
                        .getDeadline() <= curTime;
            } else {
                isTimeExpired = DateUtils.addDays(coupon.getAssignTime(),
                        coupon.getTemplateSDK().getRule().getExpiration().getGap()).getTime() <= curTime;
            }

            if (coupon.getStatus() == CouponStatus.USED) {
                used.add(coupon);
            } else if (coupon.getStatus() == CouponStatus.EXPIRED || isTimeExpired) {
                expired.add(coupon);
            } else {
                usable.add(coupon);
            }
        });
        return new CouponClassify(usable, used, expired);
    }
}
