package com.hc.common.exception;

import com.hc.common.enums.HttpCodeEnum;
import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-07 17:54
 * @description:
 */
@Data
public class ServiceException extends RuntimeException {

    private Integer status;

    private String message;

    public ServiceException(HttpCodeEnum httpCodeEnum) {
        this.status = httpCodeEnum.getStatus();
        this.message = httpCodeEnum.getMessage();
    }

    public ServiceException(String message) {
        this.status = HttpCodeEnum.CODE_500.getStatus();
        this.message = message;
    }
}
