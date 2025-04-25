package com.qx.domain.activity.service.trail.thread;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.valobj.SkuVO;

import java.util.concurrent.Callable;

/**
 * Function:
 *
 * @author 秦啸
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {


    private final String goodsId;

    private final IActivityRepository activityRepository;


    public QuerySkuVOFromDBThreadTask(String goodsId, IActivityRepository activityRepository) {
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuByGodsId(goodsId);
    }
}
