package com.qx.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRefundOrderEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 拼单组队ID
     */
    private String teamId;

    /**
     * 预购订单ID
     */
    private String orderId;


    /**
     * 活动ID
     */
    private Long activityId;

}
