package com.hc.common.lang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hc.common.enums.HttpCodeEnum;
import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-07 18:52
 * @description: 返回包装类
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer status;
    private boolean success;
    private String message;
    private T data;

    private Result(Integer status, boolean success, String message, T data) {
        this.status = status;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 操作成功不返回数据
     *
     * @param <T>
     * @return
     */
    public static <T> Result<T> success() {
        return new Result(HttpCodeEnum.CODE_200.getStatus(), true, HttpCodeEnum.CODE_200.getMessage(), null);
    }

    /**
     * 操作成功返回数据
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(HttpCodeEnum.CODE_200.getStatus(), true, HttpCodeEnum.CODE_200.getMessage(), data);
    }

    /**
     * 操作失败不返回数据
     *
     * @param status
     * @param <T>
     * @return
     */
    public static <T> Result<T> failure(int status) {
        return new Result(HttpCodeEnum.CODE_400.getStatus(), false, HttpCodeEnum.CODE_400.getMessage(), null);
    }

    /**
     * 操作失败返回数据
     *
     * @param status
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Result<T> failure(int status, String message) {
        return new Result<>(status, false, message, null);
    }
}