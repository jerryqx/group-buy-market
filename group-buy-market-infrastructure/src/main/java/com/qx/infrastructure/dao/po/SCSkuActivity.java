package com.qx.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author BEJSON.com
 * @description sc_sku_activity
 * @date 2025-04-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SCSkuActivity {

    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 来源
     */
    private String source;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}