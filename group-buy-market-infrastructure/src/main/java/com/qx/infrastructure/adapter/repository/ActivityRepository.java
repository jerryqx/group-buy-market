package com.qx.infrastructure.adapter.repository;

import com.qx.domain.activity.adapter.repository.IActivityRepository;
import com.qx.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.qx.domain.activity.model.valobj.SkuVO;
import com.qx.infrastructure.dao.IGroupBuyActivityDao;
import com.qx.infrastructure.dao.IGroupBuyDiscountDao;
import com.qx.infrastructure.dao.ISkuDao;
import com.qx.infrastructure.dao.po.GroupBuyActivity;
import com.qx.infrastructure.dao.po.GroupBuyDiscount;
import com.qx.infrastructure.dao.po.Sku;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Function:
 *
 * @author 秦啸
 */
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Resource
    private ISkuDao skuDao;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel) {
        // 根据 SC 渠道值查询配置中最新的1个有效的活动信息
        GroupBuyActivity groupBuyActivityReq = new GroupBuyActivity();
        groupBuyActivityReq.setSource(source);
        groupBuyActivityReq.setChannel(channel);
        GroupBuyActivity groupBuyActivityRes = groupBuyActivityDao.queryValidGroupBuyActivity(groupBuyActivityReq);

        if (groupBuyActivityRes != null) {
            // 根据活动ID查询活动对应的优惠券信息
            GroupBuyDiscount groupBuyDiscountRes = groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(groupBuyActivityRes.getDiscountId());

            GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName())
                .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                .discountType(groupBuyDiscountRes.getDiscountType())
                .marketPlan(groupBuyDiscountRes.getMarketPlan())
                .marketExpr(groupBuyDiscountRes.getMarketExpr())
                .tagId(groupBuyDiscountRes.getTagId())
                .build();
            return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .source(groupBuyActivityRes.getSource())
                .channel(groupBuyActivityRes.getChannel())
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
}
