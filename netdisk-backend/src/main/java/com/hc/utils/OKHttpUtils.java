package com.hc.utils;

import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: 何超
 * @date: 2023-06-14 14:02
 * @description:
 */
public class OKHttpUtils {

    private static final int TIME_OUT_SECONDS = 8;

    private static Logger logger = LoggerFactory.getLogger(OKHttpUtils.class);

    private static OkHttpClient.Builder getClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().followRedirects(false).retryOnConnectionFailure(false);
        builder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS).readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        return builder;
    }

    private static Request.Builder getRequestBuilder(Map<String, String> header) {
        Request.Builder builder = new Request.Builder();
        if (null != header) {
            for (Map.Entry<String, String> map : header.entrySet()) {
                String key = map.getKey();
                String value;
                if (map.getValue() == null) {
                    value = "";
                } else {
                    value = map.getValue();
                }
                builder.addHeader(key, value);
            }
        }
        return builder;
    }

    public static String getRequest(String url) {
        ResponseBody responseBody = null;
        try {
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            Request.Builder requestBuilder = getRequestBuilder(null);
            OkHttpClient client = clientBuilder.build();
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            String responseStr = responseBody.string();
            return responseStr;
        } catch (SocketTimeoutException | ConnectException e) {
            logger.error("请求超时");
            throw new ServiceException(HttpCodeEnum.CODE_500);
        } catch (Exception e) {
            logger.error("请求异常");
            return null;
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }
}
