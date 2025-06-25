package com.qx.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.qx.domain.trade.model.entity.*;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;
import com.qx.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.qx.infrastructure.dao.IGroupBuyActivityDao;
import com.qx.infrastructure.dao.IGroupBuyOrderDao;
import com.qx.infrastructure.dao.IGroupBuyOrderListDao;
import com.qx.infrastructure.dao.INotifyTaskDao;
import com.qx.infrastructure.dao.po.GroupBuyActivity;
import com.qx.infrastructure.dao.po.GroupBuyOrder;
import com.qx.infrastructure.dao.po.GroupBuyOrderList;
import com.qx.infrastructure.dao.po.NotifyTask;
import com.qx.infrastructure.dcc.DCCService;
import com.qx.types.common.Constants;
import com.qx.types.enums.ActivityStatusEnumVO;
import com.qx.types.enums.GroupBuyOrderEnumVO;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private INotifyTaskDao notifyTaskDao;

    @Resource
    private DCCService dccService;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderList = groupBuyOrderListDao.queryNoPayMarketPayOrderByOutTradeNo(groupBuyOrderListReq);
        if (null == groupBuyOrderList) {
            return null;

        }
        return MarketPayOrderEntity.builder().orderId(groupBuyOrderList.getOrderId()).deductionPrice(groupBuyOrderList.getDeductionPrice()).originalPrice(groupBuyOrderList.getOriginalPrice()).payPrice(groupBuyOrderList.getPayPrice()).tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.getByCode(groupBuyOrderList.getStatus())).teamId(groupBuyOrderList.getTeamId()).build();

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

            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.MINUTE, payActivityEntity.getValidTime());
            // 构建拼团订单 
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder().teamId(teamId).activityId(payActivityEntity.getActivityId()).source(payDiscountEntity.getSource()).channel(payDiscountEntity.getChannel()).originalPrice(payDiscountEntity.getOriginalPrice()).deductionPrice(payDiscountEntity.getDeductionPrice()).payPrice(payDiscountEntity.getPayPrice()).targetCount(payActivityEntity.getTargetCount()).completeCount(0).lockCount(1).validStartTime(currentDate).validEndTime(calendar.getTime()).notifyUrl(payDiscountEntity.getNotifyUrl()).build();
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
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (groupBuyOrderAggregate.getUserTaskOrderCount() + 1))
                .build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);

        }
        return MarketPayOrderEntity.builder().orderId(orderId).deductionPrice(payDiscountEntity.getDeductionPrice())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .payPrice(payDiscountEntity.getPayPrice()).tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE).teamId(teamId).build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if (null == groupBuyOrder) {
            return null;
        }
        return GroupBuyProgressVO.builder().completeCount(groupBuyOrder.getCompleteCount()).targetCount(groupBuyOrder.getTargetCount()).lockCount(groupBuyOrder.getLockCount()).build();
    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        return GroupBuyActivityEntity.builder().activityId(groupBuyActivity.getActivityId()).activityName(groupBuyActivity.getActivityName()).goodsId(groupBuyActivity.getGoodsId()).discountId(groupBuyActivity.getDiscountId()).groupType(groupBuyActivity.getGroupType()).takeLimitCount(groupBuyActivity.getTakeLimitCount()).target(groupBuyActivity.getTarget()).validTime(groupBuyActivity.getValidTime()).status(ActivityStatusEnumVO.getByCode(groupBuyActivity.getStatus())).startTime(groupBuyActivity.getStartTime()).endTime(groupBuyActivity.getEndTime()).tagId(groupBuyActivity.getTagId()).tagScope(groupBuyActivity.getTagScope()).build();
    }

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamEntity.builder().teamId(groupBuyOrder.getTeamId()).activityId(groupBuyOrder.getActivityId()).targetCount(groupBuyOrder.getTargetCount()).completeCount(groupBuyOrder.getCompleteCount()).lockCount(groupBuyOrder.getLockCount()).status(GroupBuyOrderEnumVO.getByCode(groupBuyOrder.getStatus())).validStartTime(groupBuyOrder.getValidStartTime()).validEndTime(groupBuyOrder.getValidEndTime()).notifyUrl(groupBuyOrder.getNotifyUrl()).build();
    }

    @Transactional(timeout = 500, rollbackFor = Exception.class)
    @Override
    public Boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {

        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();


        // 1. 更新拼团订单明细状态
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder().teamId(groupBuyTeamEntity.getTeamId()).userId(userEntity.getUserId()).outTradeNo(tradePaySuccessEntity.getOutTradeNo()).outTradeTime(tradePaySuccessEntity.getOutTradeTime()).build();

        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderStatus2COMPLETE(groupBuyOrderListReq);
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if (1 != updateAddCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 3. 更新拼团完成状态
        if (groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount() == 1) {
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId());
            if (1 != updateOrderStatusCount) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            // 查询拼团交易完成外部单号列表
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());

            // 拼团完成写入回调任务记录表
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyUrl(groupBuyTeamEntity.getNotifyUrl());
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isSCBlackIntercept(String source, String channel) {
        return dccService.isSCBlackIntercept(source, channel);
    }

    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {

        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder().activityId(activityId).userId(userId).build();

        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);
    }


    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        List<NotifyTask> notifyTasks = notifyTaskDao.queryUnExecutedNotifyTaskList();
        if (CollectionUtils.isEmpty(notifyTasks)) {
            return Collections.emptyList();
        }
        return notifyTasks.stream().map(notifyTask -> NotifyTaskEntity.builder().teamId(notifyTask.getTeamId()).notifyUrl(notifyTask.getNotifyUrl()).notifyCount(notifyTask.getNotifyCount()).parameterJson(notifyTask.getParameterJson()).build()).collect(Collectors.toList());
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        NotifyTask notifyTask = notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId);
        if (null == notifyTask) {
            return Collections.emptyList();
        }
        return Collections.singletonList(NotifyTaskEntity.builder().teamId(notifyTask.getTeamId()).notifyUrl(notifyTask.getNotifyUrl()).notifyCount(notifyTask.getNotifyCount()).parameterJson(notifyTask.getParameterJson()).build());
    }

    @Override
    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusSuccess(teamId);
    }

    @Override
    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusError(teamId);
    }

    @Override
    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusRetry(teamId);
    }
}
