package com.qx.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.qx.domain.trade.adapter.repository.ITradeRepository;
import com.qx.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import com.qx.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.qx.domain.trade.model.entity.*;
import com.qx.domain.trade.model.valobj.GroupBuyProgressVO;
import com.qx.domain.trade.model.valobj.NotifyConfigVO;
import com.qx.domain.trade.model.valobj.NotifyTypeEnumVO;
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
import com.qx.infrastructure.redis.IRedisService;
import com.qx.types.common.Constants;
import com.qx.types.enums.ActivityStatusEnumVO;
import com.qx.types.enums.GroupBuyOrderEnumVO;
import com.qx.types.enums.ResponseCode;
import com.qx.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
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

    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topic_team_success;

    @Resource
    private IRedisService redisService;

    @Override
    public MarketPayOrderEntity queryGroupBuyOrderRecordByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderList = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(groupBuyOrderListReq);
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
        NotifyConfigVO notifyConfigVO = payDiscountEntity.getNotifyConfigVO();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();

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
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder().teamId(teamId).activityId(payActivityEntity.getActivityId()).source(payDiscountEntity.getSource()).channel(payDiscountEntity.getChannel()).originalPrice(payDiscountEntity.getOriginalPrice()).deductionPrice(payDiscountEntity.getDeductionPrice()).payPrice(payDiscountEntity.getPayPrice()).targetCount(payActivityEntity.getTargetCount()).completeCount(0).lockCount(1).validStartTime(currentDate).validEndTime(calendar.getTime())
                    .notifyType(notifyConfigVO.getNotifyType().getCode()).notifyUrl(notifyConfigVO.getNotifyUrl()).build();
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
        GroupBuyOrderList groupBuyOrderList = GroupBuyOrderList.builder().userId(userEntity.getUserId()).teamId(teamId).orderId(orderId).activityId(payActivityEntity.getActivityId()).startTime(payActivityEntity.getStartTime()).endTime(payActivityEntity.getEndTime()).goodsId(payDiscountEntity.getGoodsId()).source(payDiscountEntity.getSource()).channel(payDiscountEntity.getChannel()).originalPrice(payDiscountEntity.getOriginalPrice()).deductionPrice(payDiscountEntity.getDeductionPrice()).payPrice(payDiscountEntity.getPayPrice()).status(TradeOrderStatusEnumVO.CREATE.getCode()).outTradeNo(payDiscountEntity.getOutTradeNo()).bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (userTakeOrderCount + 1)).build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);

        }
        return MarketPayOrderEntity.builder().orderId(orderId).deductionPrice(payDiscountEntity.getDeductionPrice()).originalPrice(payDiscountEntity.getOriginalPrice()).payPrice(payDiscountEntity.getPayPrice()).tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE).teamId(teamId).build();
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
        return GroupBuyTeamEntity.builder().teamId(groupBuyOrder.getTeamId()).activityId(groupBuyOrder.getActivityId()).targetCount(groupBuyOrder.getTargetCount()).completeCount(groupBuyOrder.getCompleteCount()).lockCount(groupBuyOrder.getLockCount()).status(GroupBuyOrderEnumVO.getByCode(groupBuyOrder.getStatus())).validStartTime(groupBuyOrder.getValidStartTime()).validEndTime(groupBuyOrder.getValidEndTime())
                .notifyConfigVO(NotifyConfigVO.builder().notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .notifyMQ(topic_team_success).build()).build();
    }

    @Transactional(timeout = 500, rollbackFor = Exception.class)
    @Override
    public NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {

        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        NotifyConfigVO notifyConfigVO = groupBuyTeamEntity.getNotifyConfigVO();
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
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyTypeEnumVO.MQ.equals(notifyConfigVO.getNotifyType()) ? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType()) ? notifyConfigVO.getNotifyUrl() : null);

            notifyTask.setNotifyCount(0);

            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);
            return NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyType(notifyTask.getNotifyType())
                    .notifyMQ(notifyTask.getNotifyMQ())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .notifyCount(notifyTask.getNotifyCount())
                    .parameterJson(notifyTask.getParameterJson())
                    .build();
        }
        return null;
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
        return notifyTasks.stream().map(notifyTask -> NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .build()).collect(Collectors.toList());
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

    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime) {
        // 失败恢复量
        Long recoveryCount = redisService.getAtomicLong(recoveryTeamStockKey);
        recoveryCount = null == recoveryCount ? 0 : recoveryCount;

        // 1. incr 得到值，与总量和恢复量做对比。恢复量为系统失败时候记录的量。
        // 2. 从有组队量开始，相当于已经有了一个占用量，所以要 +1
        // 因为失败的时候也会导致 teamStockKey 增加，所有在比较占用量的时候需要将失败的次数加上
        long occupy = redisService.incr(teamStockKey) + 1;
        if (occupy > target + recoveryCount) {
            redisService.setAtomicLong(teamStockKey, target);
            return false;
        }

        // 1. 给每个产生的值加锁为兜底设计，虽然incr操作是原子的，基本不会产生一样的值。但在实际生产中，遇到过集群的运维配置问题，以及业务运营配置数据问题，导致incr得到的值相同。
        // 2. validTime + 60分钟，是一个延后时间的设计，让数据保留时间稍微长一些，便于排查问题。
        String lockKey = teamStockKey + Constants.UNDERLINE + occupy;
        Boolean lock = redisService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);
        if (!lock) {
            log.info("组队库存加锁失败 {}", lockKey);
        }

        return lock;
    }

    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime) {
        // 首次组队拼团，是没有 teamId 的，所以不需要这个做处理。
        if (StringUtils.isBlank(recoveryTeamStockKey)) return;
        redisService.incr(recoveryTeamStockKey);
    }

    @Override
    @Transactional(timeout = 5000)
    public void unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgress = groupBuyRefundAggregate.getGroupBuyProgress();


        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        // 保留userId，企业中往往会根据 userId 作为分库分表路由键，如果将来做分库分表也可以方便处理
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());
        int updateUnpaid2RefundCount = groupBuyOrderListDao.unpaid2Refund(groupBuyOrderListReq);
        if (1 != updateUnpaid2RefundCount) {
            log.error("逆向流程，更新订单状态(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        GroupBuyOrder groupBuyOrderReq = new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgress.getLockCount());

        int updateTeamUnpaid2Refund = groupBuyOrderDao.unpaid2Refund(groupBuyOrderReq);

        if (1 != updateTeamUnpaid2Refund) {
            log.error("逆向流程，更新组队记录(退单)失败 {} {}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 逆向后，还要处理 redis recoveryCount 恢复了，这部分最后统一处理
    }


}
