package com.qx.infrastructure.dcc;

import com.qx.types.annotations.DCCValue;
import com.qx.types.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DCCService {

    /**
     * 降级开关 0 关闭 1开启
     */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    @DCCValue("scBlackList:s02c02")
    private String scBlackList;

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

    public boolean isSCBlackIntercept(String source, String channel) {
        List<String> list = Arrays.asList(scBlackList.split(Constants.SPLIT));
        return list.contains(source + channel);
    }
}
