package com.qx.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.entity.TradeRefundBehaviorEntity;
import com.qx.domain.trade.model.entity.TradeRefundCommandEntity;
import com.qx.domain.trade.model.entity.TradeRefundOrderEntity;
import com.qx.domain.trade.model.valobj.RefundTypeEnumVO;
import com.qx.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.qx.domain.trade.service.refund.business.IRefundOrderStrategy;
import com.qx.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import com.qx.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 退单节点过滤器，根据订单状态和拼团状态选择对应退单策略并执行
 */
@Slf4j
@Service
public class RefundOrderNodeFilter implements
                                   ILogicHandler<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {

    private final Map<String, IRefundOrderStrategy> refundOrderStrategyMap;

    public RefundOrderNodeFilter(Map<String, IRefundOrderStrategy> refundOrderStrategyMap) {
        this.refundOrderStrategyMap = refundOrderStrategyMap;
    }

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity,
                                           TradeRefundRuleFilterFactory.DynamicContext dynamicContext)
            throws Exception {

        log.info("逆向流程-退单操作，退单策略处理 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(),
                tradeRefundCommandEntity.getOutTradeNo());

        // 上下文数据
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();

        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO = marketPayOrderEntity.getTradeOrderStatusEnumVO();

        // 3.状态判断
        RefundTypeEnumVO refundStrategy =
                RefundTypeEnumVO.getRefundStrategy(groupBuyOrderEnumVO, tradeOrderStatusEnumVO);
        IRefundOrderStrategy refundOrderStrategy = refundOrderStrategyMap.get(refundStrategy.getStrategy());
        refundOrderStrategy.refundOrder(TradeRefundOrderEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .build());

        return TradeRefundBehaviorEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                .build();
    }
}
