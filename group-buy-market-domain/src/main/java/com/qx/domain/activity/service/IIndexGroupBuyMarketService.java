package com.qx.domain.activity.service;

import com.qx.domain.activity.model.entity.MarketProductEntity;
import com.qx.domain.activity.model.entity.TrialBalanceEntity;

/**
 * Function: 首页营销服务接口
 *
 * @author 秦啸
 */
public interface IIndexGroupBuyMarketService {

    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception;
}
