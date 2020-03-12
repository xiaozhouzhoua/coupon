package com.core.convert;

import com.core.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 优惠劵分类枚举属性转换器
 * AttributeConverter(x, y)
 * x: 是实体属性的类型
 * y: 是数据库字段的类型
 */
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory, String> {
    /**
     * 将实体属性x转换为y存储到数据库中
     */
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    /**
     * 将数据库中的字段y反序列化为实体属性x，查询操作执行的动作
     */
    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.of(code);
    }
}
