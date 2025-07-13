package com.qx.domain.trade.service.lock.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.qx.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserTaskLimitRuleFilter implements
        ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter,
                                               TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        log.info("交易规则过滤-用户参与次数校验{} activityId:{}", requestParameter.getUserId(),
                requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivityEntity = dynamicContext.getGroupBuyActivityEntity();
        Integer count =
                repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        if (null != groupBuyActivityEntity.getTakeLimitCount() && count >= groupBuyActivityEntity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }
        dynamicContext.setUserTakeOrderCount(count);
        return null;
    }
}
