package com.hc.controller;

import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.enums.VerifyRegexEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.component.RedisComponent;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.UserSpaceDto;
import com.hc.service.EmailCodeService;
import com.hc.service.UserInfoService;
import com.hc.utils.CaptchaCodeUtil;
import com.hc.utils.FileUtil;
import com.hc.utils.MD5Util;
import com.hc.utils.RandomNumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: 何超
 * @date: 2023-06-07 17:35
 * @description:
 */
@RestController
public class AccountController {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    EmailCodeService emailCodeService;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${qq.app.id}")
    private String qqAppId;

    @Value("${qq.url.authorization}")
    private String qqUrlAuthorization;

    @Value("${qq.url.redirect}")
    private String qqUrlRedirect;

    @Autowired
    RedisComponent redisComponent;

    /**
     * 生成图片验证码
     *
     * @param response
     * @param session
     * @param type     0:登录注册 1:邮箱验证码发送 默认0
     * @throws IOException
     */
    @GetMapping("/checkCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void checkCode(HttpServletResponse response, HttpSession session, @VerifyParam(required = true) Integer type) throws IOException {
        CaptchaCodeUtil vCode = new CaptchaCodeUtil(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    /**
     * 发送邮箱验证码
     *
     * @param session
     * @param email     邮箱号
     * @param checkCode 图片验证码
     * @param type      类型 0:注册 1:找回密码
     * @return
     */
    @PostMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result sendEmailCode(HttpSession session,
                                @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                @VerifyParam(required = true) String checkCode,
                                @VerifyParam(required = true) Integer type) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new ServiceException(HttpCodeEnum.CODE_405);
            }
            emailCodeService.sendEmailCode(email, type);
            return Result.success();
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 注册
     *
     * @param session
     * @param email     邮箱号
     * @param nickName  昵称
     * @param password  密码
     * @param checkCode 图片验证码
     * @param emailCode 邮箱验证码
     * @return
     */
    @PostMapping("/register")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result register(HttpSession session,
                           @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                           @VerifyParam(required = true) String nickName,
                           @VerifyParam(required = true, min = 8, max = 16) String password,
                           @VerifyParam(required = true) String checkCode,
                           @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new ServiceException(HttpCodeEnum.CODE_405);
            }
            userInfoService.register(email, nickName, password, emailCode);
            return Result.success();
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 登录
     *
     * @param session
     * @param email     邮箱
     * @param password  密码
     * @param checkCode 图片验证码
     * @return
     */
    @PostMapping("/login")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result login(HttpSession session,
                        @VerifyParam(required = true) String email,
                        @VerifyParam(required = true,min = 8,max = 16,regex = VerifyRegexEnum.PASSWORD) String password,
                        @VerifyParam(required = true) String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new ServiceException(HttpCodeEnum.CODE_405);
            }
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return Result.success(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 重置密码
     *
     * @param session
     * @param email     邮箱号
     * @param password  密码
     * @param checkCode 图片验证码
     * @param emailCode 邮箱验证码
     * @return
     */
    @PostMapping("/restPwd")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result restPwd(HttpSession session,
                          @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                          @VerifyParam(required = true, min = 8, max = 16, regex = VerifyRegexEnum.PASSWORD) String password,
                          @VerifyParam(required = true) String checkCode,
                          @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new ServiceException(HttpCodeEnum.CODE_405);
            }
            userInfoService.restPwd(email, password, emailCode);
            return Result.success();
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 获取用户头像
     *
     * @param response
     * @param userId
     */
    @GetMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) {

        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        File folder = new File(projectFolder + avatarFolderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String avatarPath = projectFolder + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File file = new File(avatarPath);
        if (!file.exists()) {
            if (!new File(projectFolder + avatarFolderName + Constants.AVATAR_DEFAULT).exists()) {
                printNoDefaultImage(response);
            }
            avatarPath = projectFolder + avatarFolderName + Constants.AVATAR_DEFAULT;
        }
        response.setContentType("image/jpg");
        FileUtil.readFile(response, avatarPath);
    }

    /**
     * 默认头像
     *
     * @param response
     */
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
            writer.close();
        } catch (Exception e) {
            logger.error("输出默认图失败", e);
        } finally {
            writer.close();
        }
    }

    /**
     * 获取用户信息
     *
     * @param session
     * @return
     */
    @GetMapping("/getUserInfo")
    @GlobalInterceptor
    public Result getUserInfo(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return Result.success(sessionWebUserDto);
    }

    /**
     * 获取用户空间
     *
     * @param session
     * @return
     */
    @PostMapping("/getUseSpace")
    @GlobalInterceptor
    public Result getUseSpace(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(sessionWebUserDto.getUserId());
        return Result.success(spaceDto);
    }

    /**
     * 退出登录
     *
     * @param session
     * @return
     */
    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    /**
     * 更新用户头像
     *
     * @param session
     * @param avatar
     * @return
     */
    @PostMapping("/updateUserAvatar")
    @GlobalInterceptor
    public Result updateUserAvatar(HttpSession session, MultipartFile avatar) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        String baseFolder = projectFolder + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        File targetFile = new File(targetFileFolder.getPath() + "/" + sessionWebUserDto.getUserId() + Constants.AVATAR_SUFFIX);
        try {
            avatar.transferTo(targetFile);
        } catch (Exception e) {
            logger.error("上传头像失败", e);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(sessionWebUserDto.getUserId());
        userInfo.setQqAvatar("");
        userInfoService.updateById(userInfo);
        sessionWebUserDto.setAvatar(null);
        session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param session
     * @param password
     * @return
     */
    @PostMapping("/updatePassword")
    @GlobalInterceptor(checkParams = true)
    public Result updatePassword(HttpSession session,
                                 @VerifyParam(required = true, min = 8, max = 16, regex = VerifyRegexEnum.PASSWORD) String password) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(sessionWebUserDto.getUserId());
        userInfo.setPassword(MD5Util.encodeByMd5(password));
        userInfoService.updateById(userInfo);
        return Result.success();
    }

    /**
     * QQ登录
     *
     * @param session
     * @param callbackUrl
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/qqlogin")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result qqlogin(HttpSession session, String callbackUrl) throws UnsupportedEncodingException {
        String state = RandomNumberUtil.getRandomNumber(Constants.LENGTH_30);
        if (!StringUtils.hasText(callbackUrl)) {
            session.setAttribute(state, callbackUrl);
        }
        String url = String.format(qqUrlAuthorization, qqAppId, URLEncoder.encode(qqUrlRedirect, "utf-8"), state);
        return Result.success(url);
    }

    /**
     * 登录回调
     * @param session
     * @param code
     * @param state
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/qqlogin/callback")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result qqlogin(HttpSession session, @VerifyParam(required = true) String code, @VerifyParam(required = true) String state) throws UnsupportedEncodingException {
        SessionWebUserDto sessionWebUserDto = userInfoService.qqLogin(code);
        session.setAttribute(Constants.SESSION_KEY,sessionWebUserDto);
        Map<String, Object> map = new HashMap<>();
        map.put("callbackUrl",session.getAttribute(state));
        map.put("userInfo",sessionWebUserDto);
        return Result.success(map);
    }
}
