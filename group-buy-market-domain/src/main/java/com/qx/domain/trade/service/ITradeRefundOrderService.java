package com.qx.domain.trade.service;

import com.qx.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.qx.domain.trade.model.entity.TradeRefundBehaviorEntity;
import com.qx.domain.trade.model.entity.TradeRefundCommandEntity;
import com.qx.domain.trade.model.valobj.TeamRefundSuccess;

import java.util.List;

/**
 * 退单服务
 */
public interface ITradeRefundOrderService {

    TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception;

    /**
     * 退单恢复锁单库存
     *
     * @param teamRefundSuccess 退单消息
     * @throws Exception 异常
     */
    void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList();
}
