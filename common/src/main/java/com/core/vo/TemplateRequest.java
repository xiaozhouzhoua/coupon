package com.core.vo;

import com.core.constant.CouponCategory;
import com.core.constant.DistributeTarget;
import com.core.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 优惠劵模板创建请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    /**
     * 优惠劵名称
     */
    private String name;

    /**
     * 优惠劵logo
     */
    private String logo;

    /**
     * 优惠劵描述
     */
    private String desc;

    /**
     * 优惠劵分类
     */
    private String category;

    /**
     * 产品线
     */
    private Integer productLine;

    /**
     * 总数
     */
    private Integer count;

    /**
     * 创建用户
     */
    private Long userId;

    /**
     * 目标用户
     */
    private Integer target;

    /**
     * 优惠劵规则
     */
    private TemplateRule rule;

    public boolean validate() {
        boolean stringValid = StringUtils.isNotEmpty(name)
                && StringUtils.isNotEmpty(logo)
                && StringUtils.isNotEmpty(desc);

        boolean enumValid = null != CouponCategory.of(category)
                && null != ProductLine.of(productLine)
                && null != DistributeTarget.of(target);

        boolean numValid = count > 0 && userId > 0;

        return stringValid && enumValid && numValid && rule.validate();
    }
}
