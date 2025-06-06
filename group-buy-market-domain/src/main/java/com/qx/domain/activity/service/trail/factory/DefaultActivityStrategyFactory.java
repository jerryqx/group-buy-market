package com.qx.domain.activity.service.trail.factory;

import com.qx.domain.activity.model.entity.MarketProductEntity;
import com.qx.domain.activity.model.entity.TrialBalanceEntity;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SkuVO;
import com.qx.domain.activity.service.trail.node.RootNode;
import com.qx.types.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Function:
 *
 * @author 秦啸
 */
@Service
public class DefaultActivityStrategyFactory {

    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        // 拼团活动营销配置对象
        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;

        // 商品信息
        private SkuVO skuVO;

        // 折扣价格
        BigDecimal deductionPrice;

        // 活动可见性限制
        private boolean visible;

        // 活动
        private boolean enable;
    }
}
