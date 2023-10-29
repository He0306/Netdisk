package com.hc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.lang.Result;
import com.hc.component.RedisComponent;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SysSettingsDto;
import com.hc.entity.query.UserInfoQuery;
import com.hc.entity.vo.UserInfoPageVO;
import com.hc.service.FileInfoService;
import com.hc.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: hec
 * @date: 2023-10-28
 * @email: 2740860037@qq.com
 * @description: 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    FileInfoService fileInfoService;

    @Autowired
    RedisComponent redisComponent;

    @Autowired
    UserInfoService userInfoService;

    /**
     * 获取系统设置信息
     *
     * @return
     */
    @PostMapping("/getSysSettings")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result getSysSettings() {
        return Result.success(redisComponent.sysSettingsDto());
    }

    /**
     * 保存系统设置信息
     *
     * @param registerEMailTitle
     * @param registerEmailContent
     * @param userInitUserSpace
     * @return
     */
    @PostMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result saveSysSettings(@VerifyParam(required = true) String registerEMailTitle, @VerifyParam(required = true) String registerEmailContent, @VerifyParam(required = true) Integer userInitUserSpace) {
        SysSettingsDto sysSettingsDto = new SysSettingsDto();
        sysSettingsDto.setRegisterEMailTitle(registerEMailTitle);
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setUserInitUserSpace(userInitUserSpace);
        redisComponent.saveSysSettingsDto(sysSettingsDto);
        return Result.success();
    }

    /**
     * 获取用户信息列表
     *
     * @param userInfoQuery
     * @return
     */
    @PostMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result loadUserList(UserInfoQuery userInfoQuery) {
        IPage<UserInfo> iPage = userInfoService.findUserInfoListByPage(userInfoQuery);
        UserInfoPageVO vo = new UserInfoPageVO();
        vo.setList(iPage.getRecords());
        vo.setPageNo(userInfoQuery.getPageNo());
        vo.setPageSize(userInfoQuery.getPageSize());
        vo.setPageTotal(iPage.getCurrent());
        vo.setTotalCount(iPage.getTotal());
        return Result.success(vo);
    }

    /**
     * 修改用户状态
     *
     * @param userId
     * @param status
     * @return
     */
    @PostMapping("updateUserStatus")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result updateUserStatus(@VerifyParam(required = true) String userId,
                                   @VerifyParam(required = true) Integer status) {
        userInfoService.updateUserInfoStatus(userId, status);
        return Result.success();
    }

    /**
     * 更新用户使用空间
     *
     * @param userId
     * @param changeSpace
     * @return
     */
    @PostMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result updateUserSpace(@VerifyParam(required = true) String userId,
                                  @VerifyParam(required = true) Integer changeSpace) {
        userInfoService.updateUserSpace(userId, changeSpace);
        return Result.success();
    }
}
