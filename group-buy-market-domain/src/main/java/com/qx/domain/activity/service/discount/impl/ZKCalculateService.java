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
        return null;
    }
}
