package com.qx.domain.activity.service.discount.impl;

import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.service.discount.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("ZK")
@Slf4j
public class ZKCalculateService extends AbstractDiscountCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice,
                                     GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {

        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());

        // 折扣表达式 - 折扣百分比
        String marketExpr = groupBuyDiscount.getMarketExpr();

        BigDecimal deductionPrice = originalPrice.multiply(new BigDecimal(marketExpr));
        // 判断折扣后金额，最低支付1分钱
        return ensureMinPay(deductionPrice);
    }
}
