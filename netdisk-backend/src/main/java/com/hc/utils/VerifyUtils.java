package com.hc.utils;

import com.hc.common.enums.VerifyRegexEnum;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 何超
 * @date: 2023-06-08 14:55
 * @description:
 */
public class VerifyUtils {

    /**
     * 正则校验参数的合格性
     *
     * @param regex 正则表达式
     * @param value 要验证的值
     * @return 校验结果
     */
    public static boolean verify(String regex, String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(value);
        return matcher.matches();
    }

    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }
}
