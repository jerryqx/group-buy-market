package com.qx.domain.activity.service.trail.node;

import com.alibaba.fastjson.JSON;
import com.qx.domain.activity.model.entity.MarketProductEntity;
import com.qx.domain.activity.model.entity.TrialBalanceEntity;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SkuVO;
import com.qx.domain.activity.service.discount.IDiscountCalculateService;
import com.qx.domain.activity.service.trail.AbstractGroupBuyMarketSupport;
import com.qx.domain.activity.service.trail.factory.DefaultActivityStrategyFactory;
import com.qx.domain.activity.service.trail.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import com.qx.domain.activity.service.trail.thread.QuerySkuVOFromDBThreadTask;
import com.qx.types.design.framework.tree.StrategyHandler;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 *
 * @author 秦啸
 */
@Slf4j
@Service
public class MarketNode extends
                        AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private TagNode tagNode;

    @Resource
    private ErrorNode errorNode;

    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Override
    protected void multiThread(MarketProductEntity requestParameter,
                               DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 异步查询活动配置信息
        QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask =
                new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getActivityId(), requestParameter.getSource(),
                        requestParameter.getChannel(), requestParameter.getGoodsId(), repository);
        FutureTask<GroupBuyActivityDiscountVO> groupBuyActivityDiscountVOFutureTask =
                new FutureTask<>(queryGroupBuyActivityDiscountVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityDiscountVOFutureTask);

        // 异步查询商品信息 - 在实际生产中，商品有同步库或者调用接口查询。这里暂时使用DB方式查询。
        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask =
                new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(), repository);
        FutureTask<SkuVO> skuVOFutureTask = new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);
        // 写入上下文 - 对于一些复杂场景，获取数据的操作，有时候会在下N个节点获取，这样前置查询数据，可以提高接口响应效率
        dynamicContext.setGroupBuyActivityDiscountVO(
                groupBuyActivityDiscountVOFutureTask.get(timeout, TimeUnit.MINUTES));
        dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.MINUTES));
        log.info("拼团商品查询试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityDiscountVO、SkuVO」完成",
                requestParameter.getUserId());
    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter,
                                      DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(),
                JSON.toJSONString(requestParameter));
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        if (null == groupBuyActivityDiscountVO) {
            return router(requestParameter, dynamicContext);
        }
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = groupBuyActivityDiscountVO.getGroupBuyDiscount();

        SkuVO skuVO = dynamicContext.getSkuVO();

        IDiscountCalculateService discountCalculateService =
                discountCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
        if (null == discountCalculateService) {
            log.info("不存在{}类型的折扣计算服务，支持类型为:{}", groupBuyDiscount.getMarketPlan(),
                    JSON.toJSONString(discountCalculateServiceMap.keySet()));
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());
        }

        BigDecimal payPrice =
                discountCalculateService.calculate(requestParameter.getUserId(), skuVO.getOriginalPrice(),
                        groupBuyDiscount);
        dynamicContext.setDeductionPrice(skuVO.getOriginalPrice().subtract(payPrice));
        dynamicContext.setPayPrice(payPrice);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(
            MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)
            throws Exception {
        if (null == dynamicContext.getGroupBuyActivityDiscountVO() || null == dynamicContext.getSkuVO() ||
                null == dynamicContext.getDeductionPrice()) {
            return errorNode;
        }
        return tagNode;
    }
}
