package com.qx.domain.activity.service.trail.node;

import com.alibaba.fastjson.JSON;
import com.qx.domain.activity.model.entity.MarketProductEntity;
import com.qx.domain.activity.model.entity.TrialBalanceEntity;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SkuVO;
import com.qx.domain.activity.service.trail.AbstractGroupBuyMarketSupport;
import com.qx.domain.activity.service.trail.factory.DefaultActivityStrategyFactory;
import com.qx.types.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Function:
 *
 * @author 秦啸
 */
@Slf4j
@Service
public class EndNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity>{


    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-EndNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        SkuVO skuVO = dynamicContext.getSkuVO();

        // 返回空结果
        return TrialBalanceEntity.builder()
            .goodsId(skuVO.getGoodsId())
            .goodsName(skuVO.getGoodsName())
            .originalPrice(skuVO.getOriginalPrice())
            .deductionPrice(dynamicContext.getDeductionPrice())
            .targetCount(groupBuyActivityDiscountVO.getTarget())
            .startTime(groupBuyActivityDiscountVO.getStartTime())
            .endTime(groupBuyActivityDiscountVO.getEndTime())
            .isVisible(false)
            .isEnable(false)
            .build();    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
