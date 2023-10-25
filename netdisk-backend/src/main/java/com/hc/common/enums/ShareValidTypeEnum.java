package com.hc.common.enums;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
public enum ShareValidTypeEnum {

    DAY_1(0, 1, "1天"),
    DAY_2(1, 7, "7天"),
    DAY_3(2, 30, "30天"),
    FOREVER(3, -1, "永久有效");

    private Integer type;

    private Integer days;

    private String desc;

    ShareValidTypeEnum(Integer type, Integer days, String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public static ShareValidTypeEnum getByType(Integer type) {
        for (ShareValidTypeEnum typeEnum : ShareValidTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public Integer getDays() {
        return days;
    }

    public String getDesc() {
        return desc;
    }
}
