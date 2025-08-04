package com.qx.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamStatisticVO {

    // 开团队伍数量
    private Integer allTeamCount;
    // 成团队伍数量
    private Integer allTeamCompleteCount;
    // 参团人数总量 - 一个商品的总参团锁单人数
    private Integer allTeamUserCount;
}
