package com.qx.domain.trade.service.refund.business;

import com.qx.domain.trade.model.entity.TradeRefundOrderEntity;

public interface IRefundOrderStrategy {

    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity);

}
