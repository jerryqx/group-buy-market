package com.qx.domain.trade.adapter.repository;

import com.qx.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.entity.NotifyTaskEntity;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.List;

public interface ITradeRepository {

    MarketPayOrderEntity queryGroupBuyOrderRecordByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCBlackIntercept(String source, String channel);

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);

    int updateNotifyTaskStatusSuccess(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusError(NotifyTaskEntity notifyTaskEntity);

    int updateNotifyTaskStatusRetry(NotifyTaskEntity notifyTaskEntity);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);

    NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    void refund2AddRecovery(String recoveryTeamStockKey, String orderId);

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();
}
