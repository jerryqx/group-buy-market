package com.qx.domain.trade.model.valobj;

import com.qx.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * 退单类型枚举
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RefundTypeEnumVO {

    UNPAID_UNLOCK("unpaid_unlock", "unpaid2RefundStrategy", "未支付，未成团") {
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) &&
                    TradeOrderStatusEnumVO.CREATE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_UNFORMED("paid_unformed", "paid2RefundStrategy", "已支付，未成团") {
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO) &&
                    TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_FORMED("paid_formed", "paidTeam2RefundStrategy", "已支付，已成团") {
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return (GroupBuyOrderEnumVO.COMPLETE.equals(groupBuyOrderEnumVO) ||
                    GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO)) &&
                    TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },
    ;

    private String code;
    private String strategy;
    private String info;

    /**
     * 抽象方法，由每个枚举值实现自己的匹配逻辑
     */
    public abstract boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO,
                                    TradeOrderStatusEnumVO tradeOrderStatusEnumVO);

    /**
     * 根据状态组合获取对应的退款策略枚举
     */
    public static RefundTypeEnumVO getRefundStrategy(GroupBuyOrderEnumVO groupBuyOrderEnumVO,
                                                     TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
        return Arrays.stream(values())
                .filter(refundType -> refundType.matches(groupBuyOrderEnumVO, tradeOrderStatusEnumVO))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "不支持的退款状态组合: groupBuyOrderStatus=" + groupBuyOrderEnumVO + ", tradeOrderStatus=" +
                                tradeOrderStatusEnumVO));
    }

    public static RefundTypeEnumVO getRefundTypeEnumVOByCode(String code) {
        return Arrays.stream(values())
                .filter(refundType -> refundType.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("不支持的退款类型: " + code));

    }
}
