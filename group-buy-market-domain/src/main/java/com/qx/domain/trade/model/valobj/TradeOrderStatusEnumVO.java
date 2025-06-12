package com.qx.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单状态
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TradeOrderStatusEnumVO {

    CREATE(0, "初始创建"),
    COMPLETE(1, "消费完成"),
    CLOSE(2, "超时关单"),
    ;

    private Integer code;
    private String info;

    public static TradeOrderStatusEnumVO getByCode(Integer code) {
        for (TradeOrderStatusEnumVO value : TradeOrderStatusEnumVO.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
