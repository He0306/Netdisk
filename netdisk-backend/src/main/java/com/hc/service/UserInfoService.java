package com.hc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.query.UserInfoQuery;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: 何超
 * @date: 2023-06-07 17:34
 * @description:
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 注册账号
     *
     * @param email     邮箱
     * @param nickName  昵称
     * @param password  密码
     * @param emailCode 邮箱验证码
     */
    void register(String email, String nickName, String password, String emailCode);

    /**
     * 登录
     *
     * @param email
     * @param password
     * @return
     */
    SessionWebUserDto login(String email, String password, HttpServletRequest request);

    /**
     * 重置密码
     *
     * @param email
     * @param password
     * @param emailCode
     */
    void restPwd(String email, String password, String emailCode);

    /**
     * qq登录
     *
     * @param code
     */
    SessionWebUserDto qqLogin(String code);

    /**
     * 分页查询所以用户
     *
     * @param userInfoQuery
     * @return
     */
    IPage<UserInfo> findUserInfoListByPage(UserInfoQuery userInfoQuery);

    /**
     * 根据用户ID更新用户状态
     *
     * @param currentUserId
     * @param userId
     * @param status
     */
    void updateUserInfoStatus(String currentUserId,String userId, Integer status);

    /**
     * 更新用户空间
     *
     * @param userId
     * @param changeSpace
     */
    void updateUserSpace(String userId, Integer changeSpace);

    /**
     * 更加用户ID删除
     *
     * @param currentUserId
     * @param userId
     */
    void deleteUser(String currentUserId, String userId);

    /**
     * 根据userId查询分片
     * @param userId
     * @return
     */
    Integer getUserChunkSizeById(String userId);

    /**
     * 更新分片大小
     * @param userId
     * @param chunkSize
     */
    void updateChunkSize(String userId, Integer chunkSize);
}
