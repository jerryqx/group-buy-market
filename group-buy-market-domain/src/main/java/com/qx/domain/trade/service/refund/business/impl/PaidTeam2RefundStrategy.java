package com.qx.domain.trade.service.refund.business.impl;

import com.qx.domain.trade.model.entity.TradeRefundOrderEntity;
import com.qx.domain.trade.service.refund.business.IRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaidTeam2RefundStrategy implements IRefundOrderStrategy {
    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {

    }
}
