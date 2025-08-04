package com.qx.domain.trade.service.refund.business.impl;

import com.alibaba.fastjson.JSON;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import com.qx.domain.trade.model.entity.NotifyTaskEntity;
import com.qx.domain.trade.model.entity.TradeRefundOrderEntity;
import com.qx.domain.trade.model.valobj.TeamRefundSuccess;
import com.qx.domain.trade.service.ITradeTaskService;
import com.qx.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.qx.domain.trade.service.refund.business.IRefundOrderStrategy;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;

@Slf4j
@Service("paid2RefundStrategy")
public class Paid2RefundStrategy implements IRefundOrderStrategy {
    @Resource
    private ITradeRepository repository;

    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单；未支付，未成团 userId:{} teamId:{} orderId:{}", tradeRefundOrderEntity.getUserId(),
                tradeRefundOrderEntity.getTeamId(), tradeRefundOrderEntity.getOrderId());

        // 1. 退单，已支付&未成团
        NotifyTaskEntity notifyTaskEntity = repository.paid2Refund(
                GroupBuyRefundAggregate.buildPaid2RefundAggregate(tradeRefundOrderEntity, -1, -1));

        // 2. 发送MQ消息
        if (null != notifyTaskEntity) {
            threadPoolExecutor.execute(() -> {
                Map<String, Integer> notifyResultMap = null;
                try {
                    notifyResultMap = tradeTaskService.execNotifyJob(notifyTaskEntity);
                    log.info("回调通知交易退单 result:{}", JSON.toJSONString(notifyResultMap));

                } catch (Exception e) {
                    log.error("回调通知交易退单失败 result:{}", JSON.toJSONString(notifyResultMap), e);
                    throw new AppException(e.getMessage());
                }
            });
        }
    }

    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess) throws Exception {
        log.info("退单；恢复锁单量 - 已支付，未成团，但有锁单记录，要恢复锁单库存 {} {} {}", teamRefundSuccess.getUserId(),
                teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
        // 1. 恢复库存key
        String recoveryTeamStockKey =
                TradeLockRuleFilterFactory.generateRecoveryTeamStockKey(teamRefundSuccess.getActivityId(),
                        teamRefundSuccess.getTeamId());
        // 2. 退单恢复「已支付，未成团，有锁单记录，要恢复锁单库存」
        repository.refund2AddRecovery(recoveryTeamStockKey, teamRefundSuccess.getOrderId());

    }
}
