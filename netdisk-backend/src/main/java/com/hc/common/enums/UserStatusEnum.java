package com.hc.common.enums;


/**
 * @author: 何超
 * @date: 2023-06-09 15:23
 * @description:
 */
public enum UserStatusEnum {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private Integer status;
    private String desc;

    UserStatusEnum(Integer status, String desc) {
        this.desc = desc;
        this.status = status;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
