package com.hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.entity.EmailCode;

/**
 * @author: 何超
 * @date: 2023-06-07 20:15
 * @description:
 */
public interface EmailCodeService extends IService<EmailCode> {

    /**
     * 发送邮箱验证码
     * @param email
     * @param type
     */
    void sendEmailCode(String email,Integer type);

    /**
     * 校验验证码
     * @param email
     * @param code
     */
    void checkCode(String email,String code);
}
