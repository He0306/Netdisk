package com.hc.common.enums;

/**
 * @author: 何超
 * @date: 2023-06-12 22:35
 * @description: 文件分类枚举
 */
public enum FileCategoryEnum {

    VIDEO(1, "video", "视频"),
    MUSIC(2, "music", "音乐"),
    IMAGE(3, "image", "图片"),
    DOC(4, "doc", "文档"),
    OTHERS(5, "others", "其它");

    private Integer category;
    private String code;
    private String desc;

    FileCategoryEnum(Integer category, String code, String desc) {
        this.category = category;
        this.code = code;
        this.desc = desc;
    }

    public static FileCategoryEnum getByCode(String code) {
        for (FileCategoryEnum item : FileCategoryEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public Integer getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }
}
