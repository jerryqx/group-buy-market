package com.qx.domain.trade.service;

import com.qx.domain.trade.model.entity.TradePaySettlementEntity;
import com.qx.domain.trade.model.entity.TradePaySuccessEntity;

public interface ITradeSettlementOrderService {

    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;

}
