package com.qx.domain.trade.service.lock;

import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.entity.*;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;
import com.qx.domain.trade.service.ITradeLockOrderService;
import com.qx.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.qx.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TradeLockOrderService implements ITradeLockOrderService {

    @Resource
    private ITradeRepository repository;

    @Resource
    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity>
            tradeRuleFilter;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易-查询未支付营销订单:{} outTradeNo:{}", userId, outTradeNo);
        return repository.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);

    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("拼团交易-查询拼单进度:{}", teamId);
        return repository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,
                                                   PayDiscountEntity payDiscountEntity) throws Exception {
        log.info("拼团交易-锁定营销优惠支付订单:{} activityId:{} goodsId:{}", userEntity.getUserId(),
                payActivityEntity.getActivityId(), payDiscountEntity.getGoodsId());
        TradeLockRuleFilterBackEntity tradeRuleFilterBackEntity = tradeRuleFilter.apply(
                TradeLockRuleCommandEntity.builder().activityId(payActivityEntity.getActivityId())
                        .userId(userEntity.getUserId()).build(), new TradeLockRuleFilterFactory.DynamicContext());
        Integer userTakeOrderCount = tradeRuleFilterBackEntity.getUserTakeOrderCount();
        // 构建聚合对象
        GroupBuyOrderAggregate groupBuyOrderAggregate =
                GroupBuyOrderAggregate.builder().userEntity(userEntity).payActivityEntity(payActivityEntity)
                        .payDiscountEntity(payDiscountEntity).userTakeOrderCount(userTakeOrderCount).build();

        // 锁定聚合订单 - 这会用户只是下单还没有支付。后续会有2个流程；支付成功、超时未支付（回退）
        return repository.lockMarketPayOrder(groupBuyOrderAggregate);
    }
}
