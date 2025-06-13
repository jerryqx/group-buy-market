package com.qx.types.enums;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ActivityStatusEnumVO {

    CREATE(0, "创建"),
    EFFECTIVE(1, "生效"),
    OVERDUE(2, "过期"),
    ABANDONED(3, "废弃"),
    ;

    private Integer code;
    private String info;

    public static ActivityStatusEnumVO getByCode(Integer code) {
        for (ActivityStatusEnumVO value : ActivityStatusEnumVO.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
