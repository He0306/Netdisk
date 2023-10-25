package com.hc.utils;

import com.hc.common.lang.Constants;

/**
 * @author: hec
 * @date: 2023-09-12
 * @email: 2740860037@qq.com
 * @description:
 */
public class StringTools {

    public static String rename(String fileName){
        String fileNameNoSuffix = getFileNameNoSuffix(fileName);
        String fileSuffix = getFileSuffix(fileName);
        return fileNameNoSuffix + "_" + RandomNumberUtil.getRandomNumber(Constants.LENGTH_5) + fileSuffix;
    }

    public static String getFileNameNoSuffix(String fileName){
        int index = fileName.lastIndexOf(".");
        if (index == -1){
            return fileName;
        }
        fileName = fileName.substring(0,index);
        return fileName;
    }

    public static String getFileSuffix(String fileName){
        int index = fileName.lastIndexOf(".");
        if (index == -1){
            return "";
        }
        return fileName.substring(index);
    }
}
