package com.qx.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderEnumVO {

    PROGRESS(0, "拼单中"),
    COMPLETE(1, "完成"),
    FAIL(2, "失败"),
    COMPLETE_FAIL(3, "完成-含退单"),
    ;

    private Integer code;
    private String info;

    public static GroupBuyOrderEnumVO getByCode(Integer code) {
        for (GroupBuyOrderEnumVO value : GroupBuyOrderEnumVO.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
