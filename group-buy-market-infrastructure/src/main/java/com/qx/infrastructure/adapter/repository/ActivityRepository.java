package com.qx.infrastructure.adapter.repository;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.qx.domain.activity.model.valobj.*;
import com.qx.infrastructure.dao.*;
import com.qx.infrastructure.dao.po.*;
import com.qx.infrastructure.dcc.DCCService;
import com.qx.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Function:
 *
 * @author 秦啸
 */
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Resource
    private ISkuDao skuDao;

    @Resource
    private IScSkuActivityDao scSkuActivityDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private DCCService dccService;

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
        // 根据 SC 渠道值查询配置中最新的1个有效的活动信息
        GroupBuyActivity groupBuyActivityRes = groupBuyActivityDao.queryValidGroupBuyActivityId(activityId);

        if (groupBuyActivityRes != null) {
            // 根据活动ID查询活动对应的优惠券信息
            GroupBuyDiscount groupBuyDiscountRes =
                    groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(groupBuyActivityRes.getDiscountId());

            GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount =
                    GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                            .discountName(groupBuyDiscountRes.getDiscountName())
                            .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                            .discountType(DiscountTypeEnum.get(groupBuyDiscountRes.getDiscountType()))
                            .marketPlan(groupBuyDiscountRes.getMarketPlan())
                            .marketExpr(groupBuyDiscountRes.getMarketExpr())
                            .tagId(groupBuyDiscountRes.getTagId())
                            .build();
            return GroupBuyActivityDiscountVO.builder()
                    .activityId(groupBuyActivityRes.getActivityId())
                    .activityName(groupBuyActivityRes.getActivityName())

                    .goodsId(groupBuyActivityRes.getGoodsId())
                    .groupBuyDiscount(groupBuyDiscount)
                    .groupType(groupBuyActivityRes.getGroupType())
                    .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                    .target(groupBuyActivityRes.getTarget())
                    .validTime(groupBuyActivityRes.getValidTime())
                    .status(groupBuyActivityRes.getStatus())
                    .startTime(groupBuyActivityRes.getStartTime())
                    .endTime(groupBuyActivityRes.getEndTime())
                    .tagId(groupBuyActivityRes.getTagId())
                    .tagScope(groupBuyActivityRes.getTagScope())
                    .build();
        }
        return null;
    }

    @Override
    public SkuVO querySkuByGodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (sku != null) {
            return SkuVO.builder()
                    .goodsId(sku.getGoodsId())
                    .goodsName(sku.getGoodsName())
                    .originalPrice(sku.getOriginalPrice())
                    .build();
        }
        return null;
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {
        SCSkuActivity scSkuActivityReq = SCSkuActivity.builder().source(source).channel(channel)
                .goodsId(goodsId).build();
        SCSkuActivity scSkuActivity =
                scSkuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if (null == scSkuActivity) return null;
        return SCSkuActivityVO.builder()
                .source(scSkuActivity.getSource())
                .chanel(scSkuActivity.getChannel())
                .activityId(scSkuActivity.getActivityId())
                .goodsId(scSkuActivity.getGoodsId())
                .build();
    }

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {

        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) return true;

        // 判断用户是否存在人群中
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }


    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, Integer ownerCount) {
        // 1. 根据用户ID、活动ID，查询用户参与的拼团队伍
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setCount(ownerCount);
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByUserId(groupBuyOrderListReq);
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) return null;

        // 2. 过滤队伍获取 TeamId,判断 teamId 是否为 null
        Set<String> teamIds =
                groupBuyOrderLists.stream().map(GroupBuyOrderList::getTeamId)
                        .filter(teamId -> !StringUtils.isBlank(teamId))
                        .collect(Collectors.toSet());

        // 3. 查询队伍明细， 组装Map 结果
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        if (null == groupBuyOrders || groupBuyOrders.isEmpty()) return null;
        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if (null == groupBuyOrder) {
                continue;
            }
            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .teamId(groupBuyOrder.getTeamId())
                    .activityId(groupBuyOrder.getActivityId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .build();
            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, Integer randomCount) {
        // 1. 根据用户ID、活动ID，查询用户参与的拼团队伍
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setActivityId(activityId);
        // 查询2倍的量，之后其中 randomCount 数量
        groupBuyOrderListReq.setCount(randomCount * 2);
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByRandom(groupBuyOrderListReq);
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) return null;
        // 判断总量是否大于 randomCount
        if (groupBuyOrderLists.size() > randomCount) {
            // 随机打乱列表
            Collections.shuffle(groupBuyOrderLists);
            // 获取前 randomCount 个元素
            groupBuyOrderLists = groupBuyOrderLists.subList(0, randomCount);
        }
        log.info("queryInProgressUserGroupBuyOrderDetailListByRandom groupBuyOrderLists: {}", groupBuyOrderLists);
        // 2. 过滤队伍获取 TeamId,判断 teamId 是否为 null
        Set<String> teamIds =
                groupBuyOrderLists.stream().map(GroupBuyOrderList::getTeamId)
                        .filter(teamId -> !StringUtils.isBlank(teamId))
                        .collect(Collectors.toSet());
        log.info("queryInProgressUserGroupBuyOrderDetailListByRandom teamIds: {}", teamIds);
        // 3. 查询队伍明细， 组装Map 结果
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        if (null == groupBuyOrders || groupBuyOrders.isEmpty()) return null;
        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if (null == groupBuyOrder) {
                continue;
            }
            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .teamId(groupBuyOrder.getTeamId())
                    .activityId(groupBuyOrder.getActivityId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .build();
            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {

        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByActivityId(activityId);

        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            return new TeamStatisticVO(0, 0, 0);
        }

        Set<String> teamIds = groupBuyOrderLists.stream().map(GroupBuyOrderList::getTeamId)
                .filter(s -> !StringUtils.isBlank(s)).collect(Collectors.toSet());

        // 3. 统计数据
        Integer allTeamCount = groupBuyOrderDao.queryAllTeamCount(teamIds);
        Integer allTeamCompleteCount = groupBuyOrderDao.queryAllTeamCompleteCount(teamIds);
        Integer allTeamUserCount = groupBuyOrderDao.queryAllUserCount(teamIds);


// 4. 构建对象
        return TeamStatisticVO.builder()
                .allTeamCount(allTeamCount)
                .allTeamCompleteCount(allTeamCompleteCount)
                .allTeamUserCount(allTeamUserCount)
                .build();
    }
}
