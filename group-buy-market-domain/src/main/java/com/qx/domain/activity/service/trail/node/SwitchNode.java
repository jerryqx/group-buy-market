package com.qx.domain.activity.service.trail.node;

import com.alibaba.fastjson.JSON;
import com.qx.domain.activity.model.entity.MarketProductEntity;
import com.qx.domain.activity.model.entity.TrialBalanceEntity;
import com.qx.domain.activity.service.trail.AbstractGroupBuyMarketSupport;
import com.qx.domain.activity.service.trail.factory.DefaultActivityStrategyFactory;
import com.qx.types.design.framework.tree.StrategyHandler;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Function:
 *
 * @author 秦啸
 */
@Slf4j
@Service
public class SwitchNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {


    @Resource
    private MarketNode marketNode;


    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-SwitchNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));

        // 根据用户ID 切量
        String userId = requestParameter.getUserId();
        if (repository.downgradeSwitch()) {
            log.info("拼团活动降级拦截 {}", userId);
            throw new AppException(ResponseCode.E0003.getCode(), ResponseCode.E0003.getInfo());
        }

        // 切量范围判断
        if (!repository.cutRange(userId)) {
            log.info("拼团活动切量拦截 {}", userId);
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }
}
