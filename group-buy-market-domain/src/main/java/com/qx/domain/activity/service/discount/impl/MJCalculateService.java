package com.qx.domain.activity.service.discount.impl;

import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.service.discount.AbstractDiscountCalculateService;
import com.qx.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("MJ")
@Slf4j
public class MJCalculateService extends AbstractDiscountCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice,
                                     GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());
        // 折扣表达式 - 100，10 满100减10
        String marketExpr = groupBuyDiscount.getMarketExpr();
        String[] split = marketExpr.split(Constants.SPLIT);
        BigDecimal x = new BigDecimal(split[0].trim());
        BigDecimal y = new BigDecimal(split[1].trim());
        // 不满足最低满减约束，则按照原价
        if (originalPrice.compareTo(x) < 0) {
            return originalPrice;
        }
        // 折扣价格
        BigDecimal deductionPrice = originalPrice.subtract(y);
        // 判断折扣后金额，最低支付1分钱
        return ensureMinPay(deductionPrice);
    }
}
