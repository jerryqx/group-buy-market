package com.qx.domain.activity.service.trail.thread;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.util.concurrent.Callable;

/**
 * Function:
 *
 * @author 秦啸
 */
public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {


    /**
     * 来源
     */
    private final String source;

    /**
     * 渠道
     */
    private final String channel;


    /**
     * 活动仓储
     */
    private IActivityRepository activityRepository;
    ;

    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.activityRepository = activityRepository;
    }

    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        return activityRepository.queryGroupBuyActivityDiscountVO(source, channel);
    }
}
