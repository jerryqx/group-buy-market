package com.qx.domain.trade.service.lock.filter;

import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.TradeRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.qx.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.qx.types.design.framework.link.model2.handler.ILogicHandler;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserTaskLimitRuleFilter implements
                                     ILogicHandler<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter,
                                           TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("交易规则过滤-用户参与次数校验{} activityId:{}", requestParameter.getUserId(),
                requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivityEntity = dynamicContext.getGroupBuyActivityEntity();
        Integer count =
                repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        if (null != groupBuyActivityEntity.getTakeLimitCount() && count >= groupBuyActivityEntity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }

        return TradeRuleFilterBackEntity.builder()
                .userTaskOrderCount(count)
                .build();
    }
}
