package com.qx.domain.trade.service.settlement.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import com.qx.domain.trade.model.entity.GroupBuyTeamEntity;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.qx.domain.trade.service.settlement.filter.EndRuleFilter;
import com.qx.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import com.qx.domain.trade.service.settlement.filter.SCRuleFilter;
import com.qx.domain.trade.service.settlement.filter.SettableRuleFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeSettlementRuleFilterFactory {


    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter(SettableRuleFilter settableRuleFilter, OutTradeNoRuleFilter outTradeNoRuleFilter, SCRuleFilter scRuleFilter, EndRuleFilter endRuleFilter) {
        // 组装链
        LinkArmory<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> linkArmory
                = new LinkArmory<>("交易结算规则过滤链", outTradeNoRuleFilter, scRuleFilter, settableRuleFilter, endRuleFilter);
        return linkArmory.getLogicLink();

    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private MarketPayOrderEntity marketPayOrderEntity;

        private GroupBuyTeamEntity groupBuyTeamEntity;

    }
}
