package com.qx.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 商品信息
 * @author BEJSON.com
 * @date 2025-03-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sku implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
    * 自增ID
    */
    private Integer id;

    /**
    * 渠道
    */
    private String source;

    /**
    * 来源
    */
    private String channel;

    /**
    * 商品ID
    */
    private String goodsId;

    /**
    * 商品名称
    */
    private String goodsName;

    /**
    * 商品价格
    */
    private BigDecimal originalPrice;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;

 }