package com.qx.infrastructure.dcc;

import com.qx.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

@Service
public class DCCService {

    /**
     * 降级开关 0 关闭 1开启
     */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        // 计算海马的绝对值
        int hashCode = Math.abs(userId.hashCode());

        // 获取最后2位
        int lastTwoDigits = hashCode % 100;

        // 判断是否在切量范围内
        return lastTwoDigits <= Integer.parseInt(cutRange);

    }
}
