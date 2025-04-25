package com.qx.domain.activity.service.trail.thread;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SCSkuActivityVO;

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
     * 商品ID
     */
    private final String goodsId;
    /**
     * 活动仓储
     */
    private final IActivityRepository activityRepository;

    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel,
                                                     String goodsId, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        SCSkuActivityVO scSkuActivityVO = activityRepository.querySCSkuActivityBySCGoodsId(source, channel, goodsId);
        if (null == scSkuActivityVO) return null;

        return activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivityVO.getActivityId());
    }
}
