package com.qx.domain.activity.service.trail;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.types.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.qx.types.design.framework.tree.AbstractStrategyRouter;

import javax.annotation.Resource;

/**
 * Function:
 *
 * @author 秦啸
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, Object, TrailBalanceEntity>
    extends AbstractMultiThreadStrategyRouter<MarketProductEntity, Object, TrailBalanceEntity> {

    protected long timeout = 500;

    @Resource
    protected IActivityRepository repository;



    @Override
    protected void multiThread(MarketProductEntity requestParameter, Object dynamicContext) throws Exception {

    }
}
