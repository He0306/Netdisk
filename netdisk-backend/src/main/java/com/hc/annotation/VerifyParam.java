package com.hc.annotation;

import com.hc.common.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: 何超
 * @date: 2023-06-08 12:59
 * @description: 校验参数
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface VerifyParam {

    int min() default -1;

    int max() default -1;

    boolean required() default false;

    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
