package com.qx.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyDiscount implements Serializable {

    private static final long serialVersionUID = 1L;

     /**
    * 自增ID
    */
    private Long id;

    /**
    * 折扣ID
    */
    private String discountId;

    /**
    * 折扣标题
    */
    private String discountName;

    /**
    * 折扣描述
    */
    private String discountDesc;

    /**
    * 折扣类型（0:base、1:tag）
    */
    private Integer discountType;

    /**
    * 营销优惠计划（ZJ:直减、MJ:满减、N元购）
    */
    private String marketPlan;

    /**
    * 营销优惠表达式
    */
    private String marketExpr;

    /**
    * 人群标签，特定优惠限定
    */
    private String tagId;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;

    public static String cacheRedisKey(String discountId) {
        return "group_buy_market_com.qx.infrastructure.dao.po.GroupBuyDiscount_" + discountId;
    }
 }