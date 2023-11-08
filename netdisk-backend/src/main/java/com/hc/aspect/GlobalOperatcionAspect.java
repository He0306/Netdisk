package com.hc.aspect;

import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.enums.VerifyRegexEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.utils.VerifyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author: 何超
 * @date: 2023-06-08 13:20
 * @description: aop前置拦截
 */
@Aspect
@Component
public class GlobalOperatcionAspect {

    // 用于判断要校验的参数是否为这几个基本类型，如果不是就当作该参数是对象来进行校验
    private static final String[] TYPE_BASE = {"java.lang.String", "java.lang.Integer", "java.lang.Long"};

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.hc.annotation.GlobalInterceptor)")
    private void requestIntercept() {

    }


    @Before("requestIntercept()")
    public void interceptorDo(JoinPoint point) {
        try {
            // 获取该切点的所在的目标对象
            Object target = point.getTarget();
            // 获取该切点所在方法的参数列表
            Object[] arguments = point.getArgs();
            // 获取该切点所在方法的方法名
            String methodName = point.getSignature().getName();
            // 获取该切点所在方法的参数列表的类型
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            // 通过方法名+参数列表的类型+反射获取该方法对象
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            // 获取该全局拦截器（也就是自定义的注解）——这一步主要是为了获取自定义注解的属性数据
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (null == interceptor) {
                return;
            }
            /**
             * 校验登录
             */
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }
            // 获取自定义注解的属性数据用来进行逻辑操作
            /**
             * 校验参数
             */
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验是否登录
     * @param checkAdmin
     */
    private void checkLogin(Boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (null == sessionWebUserDto) {
            throw new ServiceException(HttpCodeEnum.CODE_901);
        }
        if (checkAdmin && !sessionWebUserDto.getIsAdmin()) {
            throw new ServiceException(HttpCodeEnum.CODE_404);
        }
    }

    /**
     * 校验参数
     *
     * @param method    校验的方法
     * @param arguments 校验的方法对象
     */
    private void validateParams(Method method, Object[] arguments) {
        // 获取该校验方法的形参数组
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 获取一个具体的形参对象
            Parameter parameter = parameters[i];
            // 获取一个具体的实参对象
            Object value = arguments[i];
            // 找到含有@VerfyParam的参数，就是说找到要进行校验的参数
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                // 为空就说明该参数没有加该自定义注解，不需要进行校验
                continue;
            }
            // 不为空说明该参数加了该自定义注解，需要进行校验
            /* 参数校验要分两种情况
                1. 基本数据类型（及其包装类）
                2. 引用数据类型
              * */
            if (ArrayUtils.contains(TYPE_BASE, parameter.getParameterizedType().getTypeName())) {
                // 包含了就说明该参数是基本数据类型
                checkValue(value, verifyParam);
            } else {
                // 不包含就说明该参数是引用数据类型
                checkObjectValue(parameter, value);
            }
        }
    }

    /**
     * 引用数据类型校验
     *
     * @param parameter 校验对象的形参对象
     * @param object    需要校验的对象
     */
    private void checkObjectValue(Parameter parameter, Object object) {
        try {
            // 获取参数类型
            String typeName = parameter.getParameterizedType().getTypeName();
            // 获取该对象的类的字节码文件
            Class<?> classz = Class.forName(typeName);
            // 获取该类里面的所有属性对象
            Field[] fields = classz.getDeclaredFields();
            for (Field field : fields) {
                // 看该属性是否设置了校验注解
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if (fieldVerifyParam == null) {
                    // 不需要校验的属性就跳过
                    continue;
                }
                // 需要校验的属性，先跳过暴力反射确保自己能获取其值，因为一般属性都是被修饰为private的
                field.setAccessible(true);
                // 获取属性值
                Object resultValue = field.get(object);
                checkValue(resultValue, fieldVerifyParam);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 基本数据类型（及其包装类）的校验逻辑
     *
     * @param value       具体的数据值（实参对象）
     * @param verifyParam 标注该参数的注解对象（用于获取其属性数据）
     */
    private void checkValue(Object value, VerifyParam verifyParam) {
        // 首先看他加了注解是否真的想要进行校验
        if (!verifyParam.required()) {
            return;
        }
        Boolean isEmpty = value == null || "".equals(value.toString());
        Integer length = value == null ? 0 : value.toString().length();
        /**
         * 校验是否为空
         */
        if (isEmpty) {
            throw new ServiceException(HttpCodeEnum.CODE_428);
        }
        /**
         * 校验长度
         */
        if ((verifyParam.max() != -1 && verifyParam.max() < length || verifyParam.min() != -1 && verifyParam.min() > length)) {
            throw new ServiceException(HttpCodeEnum.CODE_429);
        }
        /**
         * 校验正则
         */
        if (!VerifyRegexEnum.NO.equals(verifyParam.regex()) && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new ServiceException(HttpCodeEnum.CODE_430);
        }
    }
}
