package com.qx.domain.activity.service.discount;

import com.qx.domain.activity.model.valobj.DiscountTypeEnum;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService {

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice,
                                GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        // 1. 人群标签过滤
        if (DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())) {
            boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
            if (!isCrowdRange) {
                return originalPrice;
            }
        }
        // 2. 折扣优惠计算
        return doCalculate(originalPrice, groupBuyDiscount);
    }

    // 人群过来 - 限定人群优惠
    private boolean filterTagId(String userId, String tagId) {
        // todo 后续开发
        return true;
    }

    protected abstract BigDecimal doCalculate(BigDecimal originalPrice,
                                              GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);

}
