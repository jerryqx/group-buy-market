package com.qx.domain.activity.adapter.repository;

import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SkuVO;
import com.qx.types.design.framework.tree.StrategyHandler;

/**
 * Function:
 *
 * @author 秦啸
 */
public interface IActivityRepository {

    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel);

    SkuVO querySkuByGodsId(String goodsId);
}
