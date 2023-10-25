package com.hc.common.enums;

/**
 * @author: 何超
 * @date: 2023-06-12 22:51
 * @description: 文件删除分类
 */
public enum FileDelFlagEnum {

    DEL(0, "删除"),
    RECYCLE(1, "回收站"),
    USING(2, "使用中");

    private Integer flag;
    private String desc;

    FileDelFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public Integer getFlag() {
        return flag;
    }

    public String getDesc() {
        return desc;
    }
}
