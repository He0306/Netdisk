package com.hc.common.exception;

import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.lang.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @author: 何超
 * @date: 2023-06-07 17:55
 * @description: 全局异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handleException(ServiceException serviceException) {
        return Result.failure(serviceException.getStatus(), serviceException.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException maxUploadSizeExceededException){
        return Result.failure(HttpCodeEnum.CODE_902.getStatus(), HttpCodeEnum.CODE_902.getMessage());
    }

}
