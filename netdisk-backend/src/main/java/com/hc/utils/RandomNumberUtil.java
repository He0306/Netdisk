package com.hc.utils;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author: 何超
 * @date: 2023-06-07 22:13
 * @description: 随机数
 */
public class RandomNumberUtil {

    /**
     * 生成随机数
     * @param count
     * @return
     */
    public static String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }
}
