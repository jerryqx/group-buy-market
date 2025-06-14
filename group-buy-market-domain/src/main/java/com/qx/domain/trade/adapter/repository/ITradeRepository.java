package com.qx.domain.trade.adapter.repository;

import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeRepository {

    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCBlackIntercept(String source, String channel);
}
