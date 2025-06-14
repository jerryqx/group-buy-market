package com.qx.domain.trade.service.lock.factory;

import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.qx.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.qx.domain.trade.service.lock.filter.UserTaskLimitRuleFilter;
import com.qx.types.design.framework.link.model2.LinkArmory;
import com.qx.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeLockRuleFilterFactory {

    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext,
            TradeLockRuleFilterBackEntity> tradeRuleFilter(ActivityUsabilityRuleFilter activityUsabilityRuleFilter,
                                                           UserTaskLimitRuleFilter userTaskLimitRuleFilter) {

        LinkArmory<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> linkArmory
                = new LinkArmory<>("交易规则过滤链", activityUsabilityRuleFilter, userTaskLimitRuleFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity groupBuyActivityEntity;

    }
}
