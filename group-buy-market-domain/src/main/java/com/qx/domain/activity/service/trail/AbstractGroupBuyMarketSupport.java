package com.qx.domain.activity.service.trail;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.service.trail.factory.DefaultActivityStrategyFactory;
import com.qx.types.design.framework.tree.AbstractMultiThreadStrategyRouter;

import javax.annotation.Resource;

/**
 * Function:
 *
 * @author 秦啸
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends AbstractMultiThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity>  {

    protected long timeout = 500;

    @Resource
    protected IActivityRepository repository;



    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

    }
}
