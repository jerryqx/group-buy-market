package com.qx.domain.activity.service.trail;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.service.trail.factory.DefaultActivityStrategyFactory;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Function:
 *
 * @author 秦啸
 */
@SuppressWarnings("checkstyle:LineLength")
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends
                                                                                                             AbstractMultiThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    protected long timeout = 5000;

    @Resource
    protected IActivityRepository repository;

    @Override
    protected void multiThread(MarketProductEntity requestParameter,
                               DefaultActivityStrategyFactory.DynamicContext dynamicContext)
            throws ExecutionException, InterruptedException, TimeoutException {

    }
}
