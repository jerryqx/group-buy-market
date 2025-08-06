package com.qx.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.entity.TradeRefundBehaviorEntity;
import com.qx.domain.trade.model.entity.TradeRefundCommandEntity;
import com.qx.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 数据节点过滤器，负责查询外部交易单和拼团状态数据加载
 */
@Slf4j
@Service
public class DataNodeFilter implements
                            ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity,
                                           TradeRefundRuleFilterFactory.DynamicContext dynamicContext)
            throws Exception {
        log.info("逆向流程-退单操作，数据加载节点 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(),
                tradeRefundCommandEntity.getOutTradeNo());
        // 1. 查询外部交易单，组队id、orderId、拼团状态
        MarketPayOrderEntity marketPayOrderEntity =
                repository.queryGroupBuyOrderRecordByOutTradeNo(tradeRefundCommandEntity.getUserId(),
                        tradeRefundCommandEntity.getOutTradeNo());

        // 2. 查询拼团状态
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());
        // 3. 写入上下文；如果查询数据是比较多的，可以参考 MarketNode2CompletableFuture 通过多线程进行加载
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);

        return null;
    }
}
