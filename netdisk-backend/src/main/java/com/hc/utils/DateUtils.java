package com.hc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: hec
 * @date: 2023-08-25
 * @email: 2740860037@qq.com
 * @description: 日期工具类
 */
public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String _YYYYMMDD = "yyyy/MM/dd";

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String YYYYMM = "yyyyMM";

    /**
     * 日期格式
     *
     * @param patten 格式
     * @param date   日期
     * @return 时间
     */
    public static String format(String patten, Date date) {
        return new SimpleDateFormat(patten).format(date);
    }

    /**
     * 将字符串解析成日期类
     *
     * @param patten 格式
     * @param date   时间
     * @return 日期
     */
    public static Date parse(String patten, String date) {
        try {
            return new SimpleDateFormat(patten).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getAfterDate(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, day);
        return calendar.getTime();
    }
}
