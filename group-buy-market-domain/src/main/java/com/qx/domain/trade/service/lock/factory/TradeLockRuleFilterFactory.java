package com.qx.domain.trade.service.lock.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.qx.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.qx.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import com.qx.domain.trade.service.lock.filter.UserTaskLimitRuleFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeLockRuleFilterFactory {

    private static final String teamStockKey = "group_buy_market_team_stock_key_";


    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, DynamicContext,
            TradeLockRuleFilterBackEntity> tradeRuleFilter(ActivityUsabilityRuleFilter activityUsabilityRuleFilter,
                                                           UserTaskLimitRuleFilter userTaskLimitRuleFilter,
                                                           TeamStockOccupyRuleFilter teamStockOccupyRuleFilter) {

        LinkArmory<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity> linkArmory
                = new LinkArmory<>("交易规则过滤链", activityUsabilityRuleFilter, userTaskLimitRuleFilter,
                teamStockOccupyRuleFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private String teamStockKey = "group_buy_market_team_stock_key_";

        private GroupBuyActivityEntity groupBuyActivityEntity;

        private Integer userTakeOrderCount;

        public String generateTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return TradeLockRuleFilterFactory.generateTeamStockKey(groupBuyActivityEntity.getActivityId(), teamId);
        }

        public String generateRecoveryTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return TradeLockRuleFilterFactory.generateRecoveryTeamStockKey(groupBuyActivityEntity.getActivityId(),
                    teamId);
        }
    }

    public static String generateTeamStockKey(Long activityId, String teamId) {
        return teamStockKey + activityId + "_" + teamId;
    }

    public static String generateRecoveryTeamStockKey(Long activityId, String teamId) {
        return teamStockKey + activityId + "_" + teamId + "_recovery";
    }
}
