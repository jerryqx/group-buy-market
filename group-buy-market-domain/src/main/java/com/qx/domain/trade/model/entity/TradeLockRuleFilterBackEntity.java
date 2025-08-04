package com.qx.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleFilterBackEntity {

    private Integer userTakeOrderCount;

    // 恢复组队库存缓存key
    private String recoveryTeamStockKey;
}
