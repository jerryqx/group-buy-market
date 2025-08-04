package com.qx.domain.activity.service.discount;

import com.alibaba.fastjson.JSON;
import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.valobj.DiscountTypeEnum;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService {

    @Resource
    private IActivityRepository repository;

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice,
                                GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        // 1. 人群标签过滤
        if (DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())) {
            boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
            if (!isCrowdRange) {
                log.info("折扣计算优惠拦截 userId:{}, 折扣详情: {}", userId, JSON.toJSONString(groupBuyDiscount));
                return originalPrice;
            }
        }
        // 2. 折扣优惠计算
        return doCalculate(originalPrice, groupBuyDiscount);
    }

    // 人群过来 - 限定人群优惠
    private boolean filterTagId(String userId, String tagId) {

        return repository.isTagCrowdRange(tagId, userId);
    }

    protected abstract BigDecimal doCalculate(BigDecimal originalPrice,
                                              GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);

    protected BigDecimal ensureMinPay(BigDecimal deductionPrice) {
        if (deductionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.01");
        }
        return deductionPrice;
    }
}
