package com.qx.infrastructure.adapter.repository;

import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.entity.MarketPayOrderEntity;
import com.qx.domain.trade.model.entity.PayActivityEntity;
import com.qx.domain.trade.model.entity.PayDiscountEntity;
import com.qx.domain.trade.model.entity.UserEntity;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;
import com.qx.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.qx.infrastructure.dao.IGroupBuyOrderDao;
import com.qx.infrastructure.dao.IGroupBuyOrderListDao;
import com.qx.infrastructure.dao.po.GroupBuyOrder;
import com.qx.infrastructure.dao.po.GroupBuyOrderList;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderList = groupBuyOrderListDao.queryNoPayMarketPayOrderByOutTradeNo(groupBuyOrderListReq);
        if (null == groupBuyOrderList) {
            return null;

        }
        return
                MarketPayOrderEntity.builder().orderId(groupBuyOrderList.getOrderId())
                        .deductionPrice(groupBuyOrderList.getDeductionPrice())
                        .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.getByCode(groupBuyOrderList.getStatus()))
                        .build();

    }

    @Transactional(timeout = 500, rollbackFor = Exception.class)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {

        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();

        // 判断是否已经有团  teamId 为空 - 新团  为不空 - 老团
        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
            teamId = RandomStringUtils.randomNumeric(8);

            // 构建拼团订单 
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payDiscountEntity.getDeductionPrice())
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .lockCount(1)
                    .build();
            // 写入记录
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            int updateAddTargetCount = groupBuyOrderDao.updateAddLockCount(teamId);
            if (1 != updateAddTargetCount) {
                throw new AppException(ResponseCode.E0005);
            }
        }

        // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderList = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);

        }
        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE).build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if (null == groupBuyOrder) {
            return null;
        }
        return GroupBuyProgressVO.builder()
                .completeCount(groupBuyOrder.getCompleteCount())
                .targetCount(groupBuyOrder.getTargetCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }
}
