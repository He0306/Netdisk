package com.hc.common.enums;

/**
 * @author: 何超
 * @date: 2023-06-08 13:05
 * @description:
 */

/**
 * @author wzx
 * 定义正则枚举
 */

public enum VerifyRegexEnum {

    NO("", "不校验"),
    EMAIL("^[a-zA-Z0-9]+([-_.][a-zA-Z0-9]+)*@[qQ][qQ]\\.com$", "邮箱"),
    PASSWORD("^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,18}$", "只能是数字，字母，特殊字符 8-18位");

    private String regex;
    private String desc;

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}
