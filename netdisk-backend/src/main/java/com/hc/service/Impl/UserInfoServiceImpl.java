package com.hc.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.enums.UserStatusEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.component.RedisComponent;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.QQInfoDto;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.SysSettingsDto;
import com.hc.entity.dto.UserSpaceDto;
import com.hc.entity.query.UserInfoQuery;
import com.hc.mapper.FileInfoMapper;
import com.hc.mapper.ShareMapper;
import com.hc.mapper.UserInfoMapper;
import com.hc.service.EmailCodeService;
import com.hc.service.UserInfoService;
import com.hc.utils.JsonUtil;
import com.hc.utils.MD5Util;
import com.hc.utils.OKHttpUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * @author: 何超
 * @date: 2023-06-07 17:34
 * @description:
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    EmailCodeService emailCodeService;

    @Autowired
    ShareMapper shareMapper;

    @Autowired
    RedisComponent redisComponent;

    @Value("${admin.emails}")
    private String adminEmail;

    @Value("${qq.app.key}")
    private String qqAppKey;

    @Value("${qq.url.access.token}")
    private String qqUrlAccessToken;

    @Value("${qq.url.openid}")
    private String qqUrlOpenId;

    @Value("${qq.url.user.info}")
    private String qqUrlUserInfo;

    @Value("${qq.app.id}")
    private String qqAppId;

    @Value("${qq.url.redirect}")
    private String qqUrlRedirect;

    @Autowired
    FileInfoMapper fileInfoMapper;

    /**
     * 注册账号
     *
     * @param email     邮箱
     * @param nickName  昵称
     * @param password  密码
     * @param emailCode 邮箱验证码
     */
    @Override
    public void register(String email, String nickName, String password, String emailCode) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new ServiceException(HttpCodeEnum.CODE_407);
        }
        UserInfo nickNameUser = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getNickName, nickName));
        if (null != nickNameUser) {
            throw new ServiceException(HttpCodeEnum.CODE_408);
        }
        // 校验邮箱验证码
        emailCodeService.checkCode(email, emailCode);
        userInfo = new UserInfo();
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(MD5Util.encodeByMd5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setUserSpace(0L);
        SysSettingsDto sysSettingsDto = redisComponent.sysSettingsDto();
        userInfo.setTotalSpace(sysSettingsDto.getUserInitUserSpace() * Constants.MB);
        userInfoMapper.insert(userInfo);
    }

    /**
     * 登录
     *
     * @param email    邮箱号
     * @param password 密码
     * @return
     */
    @Override
    public SessionWebUserDto login(String email, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (null == userInfo || !MD5Util.encodeByMd5(password).equals(userInfo.getPassword())) {
            throw new ServiceException(HttpCodeEnum.CODE_409);
        }
        if (userInfo.getStatus() == 0) {
            throw new ServiceException(HttpCodeEnum.CODE_410);
        }
        // 更新最后登录时间
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.updateById(userInfo);

        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(userInfo.getUserId());
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setAvatar(userInfo.getQqAvatar());
        if (ArrayUtils.contains(adminEmail.split(","), email)) {
            sessionWebUserDto.setIsAdmin(true);
        } else {
            sessionWebUserDto.setIsAdmin(false);
        }
        // 用户空间
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(fileInfoMapper.selectUserSpace(userInfo.getUserId()));
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
        return sessionWebUserDto;
    }

    /**
     * 重置密码
     *
     * @param email
     * @param password
     * @param emailCode
     */
    @Override
    public void restPwd(String email, String password, String emailCode) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (null == userInfo) {
            throw new ServiceException(HttpCodeEnum.CODE_406);
        }
        emailCodeService.checkCode(email, emailCode);
        userInfo.setPassword(MD5Util.encodeByMd5(password));
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 更新用户空间
     *
     * @param userId
     * @param changeSpace
     */
    @Override
    public void updateUserSpace(String userId, Integer changeSpace) {
        Long space = changeSpace * Constants.MB;
        userInfoMapper.updateUserSpaceInteger(userId, null, space);
        redisComponent.restUserSpaceUse(userId);
    }

    /**
     * 根据用户ID更新用户状态
     *
     * @param currentUserId
     * @param userId
     * @param status
     */
    @Override
    public void updateUserInfoStatus(String currentUserId,String userId, Integer status) {
        if (currentUserId.equals(userId)){
            throw new ServiceException(HttpCodeEnum.CODE_427);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(status);
        if (UserStatusEnum.DISABLE.getStatus().equals(status)) {
            userInfo.setUserSpace(0L);
            fileInfoMapper.deleteByUserId(userId);
        }
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 更加用户ID删除
     *
     * @param currentUserId
     * @param userId
     */
    @Override
    public void deleteUser(String currentUserId, String userId) {
        if (currentUserId.equals(userId)) {
            throw new ServiceException(HttpCodeEnum.CODE_427);
        }
        shareMapper.deleteByUserId(userId);
        fileInfoMapper.deleteByUserId(userId);
        userInfoMapper.deleteById(userId);
    }

    /**
     * 分页查询所以用户
     *
     * @param userInfoQuery
     * @return
     */
    @Override
    public IPage<UserInfo> findUserInfoListByPage(UserInfoQuery userInfoQuery) {
        Page<UserInfo> page = new Page<>(userInfoQuery.getPageNo(), userInfoQuery.getPageSize());
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(userInfoQuery.getNickName())) {
            wrapper.like(UserInfo::getNickName, userInfoQuery.getNickName());
        }
        if (userInfoQuery.getStatus() != null) {
            wrapper.eq(UserInfo::getStatus, userInfoQuery.getStatus());
        }
        wrapper.orderByDesc(UserInfo::getJoinTime);
        return userInfoMapper.selectPage(page, wrapper);
    }

    /**
     * qq登录
     *
     * @param code
     */
    @Override
    public SessionWebUserDto qqLogin(String code) {
        // 第一步  通过回调code 获取accessToken
        String accessToken = getQQAccessToken(code);
        // 第二步  获取qq openID
        String openId = getQQOpenId(accessToken);
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getQqOpenId, openId);
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        String avatar = null;
        if (null == userInfo) {
            // 自动注册登录
            // 第三步  获取qq用户信息
            QQInfoDto qqUserInfo = getQQUserInfo(accessToken, openId);
            userInfo = new UserInfo();
            String nickName = qqUserInfo.getNickName();
            nickName = nickName.length() > Constants.LENGTH_20 ? nickName.substring(0, Constants.LENGTH_20) : nickName;
            avatar = StringUtils.isEmpty(qqUserInfo.getFigureUrl_qq_2()) ? qqUserInfo.getFigureUrl_qq_1() : qqUserInfo.getFigureUrl_qq_2();
            Date curDate = new Date();
            userInfo.setQqOpenId(openId);
            userInfo.setJoinTime(curDate);
            userInfo.setNickName(nickName);
            userInfo.setQqAvatar(avatar);
            userInfo.setLastLoginTime(curDate);
            userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
            userInfo.setUserSpace(fileInfoMapper.selectUserSpace(userInfo.getUserId()));
            userInfo.setTotalSpace(redisComponent.sysSettingsDto().getUserInitUserSpace() * Constants.MB);
            userInfoMapper.insert(userInfo);
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getQqOpenId, openId);
            userInfo = userInfoMapper.selectOne(queryWrapper);
        } else {
            UserInfo updateInfo = new UserInfo();
            updateInfo.setLastLoginTime(new Date());
            updateInfo.setUserId(userInfo.getUserId());
            avatar = userInfo.getQqAvatar();
            updateInfo.setQqAvatar(avatar);
            userInfoMapper.updateById(updateInfo);
        }
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(userInfo.getUserId());
        sessionWebUserDto.setAvatar(userInfo.getQqAvatar());
        sessionWebUserDto.setNickName(userInfo.getNickName());
        if (ArrayUtils.contains(adminEmail.split(","), userInfo.getEmail() == null ? "" : userInfo.getEmail())) {
            sessionWebUserDto.setIsAdmin(true);
        } else {
            sessionWebUserDto.setIsAdmin(false);
        }
        // 用户空间
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(fileInfoMapper.selectUserSpace(userInfo.getUserId()));
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
        return sessionWebUserDto;
    }

    private QQInfoDto getQQUserInfo(String accessToken, String qqOpenId) {
        String url = String.format(qqUrlUserInfo, accessToken, qqAppId, qqOpenId);
        String response = OKHttpUtils.getRequest(url);
        if (StringUtils.isNotBlank(response)) {
            QQInfoDto qqInfoDto = JsonUtil.convertJson2Obj(response, QQInfoDto.class);
            if (qqInfoDto.getRet() != 0) {
                throw new ServiceException(HttpCodeEnum.CODE_415);
            }
            return qqInfoDto;
        }
        throw new ServiceException(HttpCodeEnum.CODE_415);
    }

    private String getQQAccessToken(String code) {
        String accessToken = null;
        String url = null;
        try {
            url = String.format(qqUrlAccessToken, qqAppId, qqAppKey, code, URLEncoder.encode(qqUrlRedirect, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode失败");
        }
        String tokenResult = OKHttpUtils.getRequest(url);
        if (tokenResult == null || tokenResult.indexOf(Constants.VIEW_OBJ_RESULT_LEY) != -1) {
            throw new ServiceException(HttpCodeEnum.CODE_406);
        }
        String[] params = tokenResult.split("&");
        if (params != null && params.length > 0) {
            for (String param : params) {
                if (param.indexOf("access_token") != -1) {
                    accessToken = param.split("=")[1];
                    break;
                }
            }
        }
        return accessToken;
    }

    private String getQQOpenId(String accessToken) {
        String url = String.format(qqUrlOpenId, accessToken);
        String openIDResult = OKHttpUtils.getRequest(url);
        String tmpJson = getQQResp(openIDResult);
        if (tmpJson == null) {
            throw new ServiceException(HttpCodeEnum.CODE_414);
        }
        Map jsonData = JsonUtil.convertJson2Obj(tmpJson, Map.class);
        if (jsonData == null || jsonData.containsKey(Constants.VIEW_OBJ_RESULT_LEY)) {
            throw new ServiceException(HttpCodeEnum.CODE_414);
        }
        return String.valueOf(jsonData.get("openid"));
    }

    private String getQQResp(String result) {
        if (StringUtils.isNotBlank(result)) {
            int pos = result.indexOf("callback");
            if (pos != -1) {
                int start = result.indexOf("(");
                int end = result.indexOf(")");
                String jsonStr = result.substring(start + 1, end + 1);
                return jsonStr;
            }
        }
        return null;
    }
}
