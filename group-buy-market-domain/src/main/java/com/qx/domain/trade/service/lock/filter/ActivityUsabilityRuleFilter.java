package com.qx.domain.trade.service.lock.filter;

import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.entity.GroupBuyActivityEntity;
import com.qx.domain.trade.model.entity.TradeRuleCommandEntity;
import com.qx.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.qx.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.qx.types.design.framework.link.model2.handler.ILogicHandler;
import com.qx.types.enums.ActivityStatusEnumVO;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class ActivityUsabilityRuleFilter implements
                                         ILogicHandler<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter,
                                           TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-活动的可用性校验{} activityId:{}", requestParameter.getUserId(),
                requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivityEntity =
                repository.queryGroupBuyActivityByActivityId(requestParameter.getActivityId());

        // 校验；活动状态 - 可以抛业务异常code，或者把code写入到动态上下文dynamicContext中，最后获取。
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivityEntity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivityEntity.getStartTime()) ||
                currentTime.after(groupBuyActivityEntity.getEndTime())) {
            throw new AppException(ResponseCode.E0102);
        }
        dynamicContext.setGroupBuyActivityEntity(groupBuyActivityEntity);
        return null;
    }
}
