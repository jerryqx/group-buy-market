package com.qx.domain.trade.model.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TradeRefundCommandEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /**
     * 渠道
     */
    private String source;

    /**
     * 来源
     */
    private String channel;
}
