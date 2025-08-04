package com.qx.domain.trade.service.refund.business;

import com.qx.domain.trade.model.entity.TradeRefundOrderEntity;
import com.qx.domain.trade.model.valobj.TeamRefundSuccess;

public interface IRefundOrderStrategy {

    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity);

    void reverseStock(TeamRefundSuccess teamRefundSuccess) throws Exception;

}
