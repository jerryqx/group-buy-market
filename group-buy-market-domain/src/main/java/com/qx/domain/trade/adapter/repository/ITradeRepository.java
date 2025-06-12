package com.qx.domain.trade.adapter.repository;

import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeRepository {

    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

}
