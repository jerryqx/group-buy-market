package com.qx.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.qx.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class EndRuleFilter implements
                           ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter,
                                                     TradeSettlementRuleFilterFactory.DynamicContext dynamicContext)
            throws Exception {
        log.info("结算规则过滤-结束节点{} outTradeNo:{}", requestParameter.getUserId(),
                requestParameter.getOutTradeNo());

        // 获取上下文对象
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();

        // 返回封装数据
        return TradeSettlementRuleFilterBackEntity.builder()
                .teamId(groupBuyTeamEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .targetCount(groupBuyTeamEntity.getTargetCount())
                .completeCount(groupBuyTeamEntity.getCompleteCount())
                .lockCount(groupBuyTeamEntity.getLockCount())
                .status(groupBuyTeamEntity.getStatus())
                .validStartTime(groupBuyTeamEntity.getValidStartTime())
                .validEndTime(groupBuyTeamEntity.getValidEndTime())
                .notifyConfigVO(groupBuyTeamEntity.getNotifyConfigVO())
                .build();
    }
}
