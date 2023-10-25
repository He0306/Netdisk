package com.hc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.entity.EmailCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: 何超
 * @date: 2023-06-07 20:14
 * @description:
 */
@Mapper
public interface EmailCodeMapper extends BaseMapper<EmailCode> {

    /**
     * 更新验证码为禁用
     * @param email
     */
    void disableEmailCode(@Param("email") String email);

    /**
     * 校验验证码
     * @param email
     * @param code
     * @return
     */
    EmailCode selectByEmailAndCode(@Param("email") String email,@Param("code") String code);
}
