package com.hc.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


/**
 * @author: 何超
 * @date: 2023-06-07 22:34
 * @description:
 */
@Component
public class RedisUtil<V> {

    @Resource
    RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean delKey(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            logger.error("删除redisKey:{}失败", key);
            return false;
        }
    }
}
