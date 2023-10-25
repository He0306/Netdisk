package com.hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SessionWebUserDto;

/**
 * @author: 何超
 * @date: 2023-06-07 17:34
 * @description:
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 注册账号
     * @param email 邮箱
     * @param nickName 昵称
     * @param password 密码
     * @param emailCode 邮箱验证码
     */
    void register(String email,String nickName,String password,String emailCode);

    /**
     * 登录
     * @param email
     * @param password
     * @return
     */
    SessionWebUserDto login(String email,String password);

    /**
     * 重置密码
     * @param email
     * @param password
     * @param emailCode
     */
    void restPwd(String email, String password, String emailCode);

    /**
     * qq登录
     * @param code
     */
    SessionWebUserDto qqLogin(String code);
}
