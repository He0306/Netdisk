package com.hc.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.hc.annotation.Limit;
import com.hc.common.exception.ServiceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author: hec
 * @date: 2023-11-08
 * @email: 2740860037@qq.com
 * @description:
 */
@Aspect
@Component
public class LimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(LimitAspect.class);

    private final Map<String, RateLimiter> limiterMap = Maps.newConcurrentMap();

    /**
     * 环绕通知
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.hc.annotation.Limit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 拿limit注解
        Limit limit = method.getAnnotation(Limit.class);
        if (limit != null) {
            // key作用：不同的接口，不同的流量规则
            String key = limit.key();
            RateLimiter rateLimiter;
            // 验证缓存是否有命中key
            if (!limiterMap.containsKey(key)) {
                // 创建令牌桶
                rateLimiter = RateLimiter.create(limit.permitsPerSecond());
                limiterMap.put(key, rateLimiter);
                logger.info("新建了令牌桶={}，容量={}", key, limit.permitsPerSecond());
            }
            rateLimiter = limiterMap.get(key);
            // 拿令牌
            boolean acquire = rateLimiter.tryAcquire(limit.timeout(), limit.timeunit());
            // 拿不到命令，直接返回异常提示
            if (!acquire) {
                logger.error("令牌桶={}，获取令牌失败", key);
                throw new ServiceException(limit.msg());
            }
        }
        return point.proceed();
    }
}
