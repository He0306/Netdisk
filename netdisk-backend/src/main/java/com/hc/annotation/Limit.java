package com.hc.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: hec
 * @date: 2023-11-08
 * @email: 2740860037@qq.com
 * @description: 接口限流
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Limit {

    /**
     * 资源key
     *
     * @return
     */
    String key() default "";

    /**
     * 最多访问次数
     *
     * @return
     */
    double permitsPerSecond();

    /**
     * 限制时间
     *
     * @return
     */
    long timeout();

    /**
     * 时间类型
     *
     * @return
     */
    TimeUnit timeunit() default TimeUnit.MINUTES;

    /**
     * 提示信息
     *
     * @return
     */
    String msg() default "请求次数过多，请稍后再试！";
}
