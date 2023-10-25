package com.hc.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author: 何超
 * @date: 2023-06-14 14:37
 * @description:
 */
public class JsonUtil {

    public static String convertObj2Json(Object obj){
        return JSON.toJSONString(obj);
    }

    public static <T> T convertJson2Obj(String json,Class<T> classz){
        return JSONObject.parseObject(json,classz);
    }

    public static <T> List<T> convertJsonArray2List(String json, Class<T> classz){
        return JSONArray.parseArray(json,classz);
    }
}
