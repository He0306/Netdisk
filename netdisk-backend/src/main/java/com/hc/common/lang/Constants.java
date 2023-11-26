package com.hc.common.lang;

/**
 * @author: 何超
 * @date: 2023-06-07 18:54
 * @description:
 */
public interface Constants {

    public static final String VIEW_OBJ_RESULT_LEY = "result";
    public static final String IMAGE_PNG_SUFFIX = ".png";

    /**
     * 默认头像
     */
    public static final String AVATAR_DEFAULT = "default_avatar.jpg";

    /**
     * 头像后缀
     */
    public static final String AVATAR_SUFFIX = ".jpg";

    /**
     * 文件目录
     */
    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_TEMP = "/temp/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    /**
     * 兆
     */
    public static final Long MB = 1024 * 1024L;

    /**
     * 验证码key
     */
    public static final String CHECK_CODE_KEY = "CHECK_CODE_KEY";

    /**
     * 邮箱key
     */
    public static final String CHECK_CODE_KEY_EMAIL = "CHECK_CODE_KEY_EMAIL";

    /**
     * 6位随机数
     */
    public static final Integer LENGTH_6 = 6;

    /**
     * 验证码五分钟
     */
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_30 = 30;
    public static final Integer LENGTH_50 = 50;
    public static final Integer LENGTH_20 = 20;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_150 = 150;

    /**
     * 常量0
     */
    public static final Integer ZERO = 0;
    public static final String ZERO_STR = "0";

    /**
     * 存储发送验证码内容
     */
    public static final String REDIS_KEY_SYS_SETTING = "yunPan:sysSetting:";
    public static final String REDIS_KEY_DOWNLOAD = "yunPan:download:";

    public static final String REDIS_KEY_USER_FILE_TEMP_SIZE = "yunPan:user:file:temp";
    /**
     * 用户已使用的空间
     */
    public static final String REDIS_KEY_USER_SPACE_USE = "yunPan:user:spaceUse:";

    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;
    public static final Integer REDIS_KEY_EXPIRES_HOUR = REDIS_KEY_EXPIRES_ONE_MIN * 60;
    public static final Integer REDIS_KEY_EXPIRES_HALF_AN_HOUR = REDIS_KEY_EXPIRES_ONE_MIN * 30;

    /**
     * 一天过期
     */
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    public static final String SESSION_KEY = "session_key";
    public static final String SESSION_SHARE_KEY = "session_share_key";
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "m3u8";


}
