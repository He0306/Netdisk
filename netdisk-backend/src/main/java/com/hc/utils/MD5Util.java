package com.hc.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author: 何超
 * @date: 2023-06-09 15:15
 * @description: 密码加密工具类
 */
public class MD5Util {

    public static String encodeByMd5(String originString) {
        return DigestUtils.md5Hex(originString);
    }
}
