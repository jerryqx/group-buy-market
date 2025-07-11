package com.qx.domain.trade.model.entity;


import com.qx.domain.trade.model.valobj.NotifyConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayDiscountEntity {

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
     * 原始价格
     */
    private BigDecimal originalPrice;
    /**
     * 折扣金额
     */
    private BigDecimal deductionPrice;
    /**
     * 支付金额
     */
    BigDecimal payPrice;
    /**
     * 外部交易单号-确保外部调用唯一幂等
     */
    private String outTradeNo;

    /**
     * 回调地址
     */
    private NotifyConfigVO notifyConfigVO;
}
