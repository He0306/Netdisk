package com.hc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.component.RedisComponent;
import com.hc.entity.FileInfo;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.SysSettingsDto;
import com.hc.entity.query.FileInfoQuery;
import com.hc.entity.query.UserInfoQuery;
import com.hc.entity.vo.FileInfoPageVO;
import com.hc.entity.vo.UserInfoPageVO;
import com.hc.service.FileInfoService;
import com.hc.service.UserInfoService;
import com.hc.utils.PreviewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.List;

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

    @Autowired
    PreviewUtils previewUtils;

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
     * @param registerEmailTitle
     * @param registerEmailContent
     * @param userInitUserSpace
     * @return
     */
    @PostMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result saveSysSettings(@VerifyParam(required = true) String registerEmailTitle, @VerifyParam(required = true) String registerEmailContent, @VerifyParam(required = true) Integer userInitUserSpace) {
        SysSettingsDto sysSettingsDto = new SysSettingsDto();
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
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
     * @param session
     * @param userId
     * @param status
     * @return
     */
    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result updateUserStatus(HttpSession session,@VerifyParam(required = true) String userId, @VerifyParam(required = true) Integer status) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        userInfoService.updateUserInfoStatus(sessionWebUserDto.getUserId(),userId, status);
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
    public Result updateUserSpace(@VerifyParam(required = true) String userId, @VerifyParam(required = true) Integer changeSpace) {
        userInfoService.updateUserSpace(userId, changeSpace);
        return Result.success();
    }

    /**
     * 根据用户ID删除
     *
     * @param session
     * @param userId
     * @return
     */
    @PostMapping("/deleteUser")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result deleteUser(HttpSession session, @VerifyParam(required = true) String userId) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        userInfoService.deleteUser(sessionWebUserDto.getUserId(), userId);
        return Result.success();
    }

    /**
     * 查询所有文件
     *
     * @param query
     * @return
     */
    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public Result loadFileList(FileInfoQuery query) {
        PageInfo<FileInfo> fileInfoIPage = fileInfoService.findAdminFileInfoListByPage(query);
        FileInfoPageVO fileInfoPageVO = new FileInfoPageVO();
        fileInfoPageVO.setList(fileInfoIPage.getList());
        fileInfoPageVO.setPageNo(query.getPageNo());
        fileInfoPageVO.setPageSize(query.getPageSize());
        fileInfoPageVO.setPageTotal(Long.valueOf(fileInfoIPage.getPrePage()));
        fileInfoPageVO.setTotalCount(fileInfoIPage.getTotal());
        return Result.success(fileInfoPageVO);
    }

    /**
     * 预览文件
     *
     * @param response
     * @param userId
     * @param fileId
     */
    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getFile(HttpServletResponse response,
                        @PathVariable("userId") String userId,
                        @PathVariable("fileId") String fileId) {
        previewUtils.getFile(response, fileId, userId);
    }

    /**
     * 视频预览
     *
     * @param response
     * @param userId
     * @param fileId
     */
    @GetMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getVideo(HttpServletResponse response,
                         @PathVariable("userId") String userId,
                         @PathVariable("fileId") String fileId) {
        previewUtils.getVideo(response, fileId, userId);
    }

    /**
     * 获取对应文件夹的信息
     *
     * @param path
     * @return
     */
    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public Result newFolder(@VerifyParam(required = true) String path) {
        List<FileInfo> folderInfo = previewUtils.getFolderInfo(path, null);
        return Result.success(folderInfo);
    }

    /**
     * 创建下载链接
     *
     * @param userId
     * @param fileId
     * @return
     */
    @PostMapping("/createDownLoadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public Result createDownLoadUrl(@VerifyParam(required = true) @PathVariable(value = "fileId") String fileId,
                                    @PathVariable("userId") String userId) {
        String code = fileInfoService.createDownLoadUrl(fileId, userId);
        return Result.success(code);
    }

    /**
     * 下载文件
     *
     * @param response
     * @param request
     * @param code
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void download(HttpServletResponse response, HttpServletRequest request, @VerifyParam(required = true) @PathVariable(value = "code") String code) throws UnsupportedEncodingException {
        fileInfoService.download(request, response, code);
    }

    @PostMapping("")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public Result adminDelete(@VerifyParam(required = true) String fileIdAndUserIds){
        String[] fileIdAndUserIdsArray = fileIdAndUserIds.split(",");
        for (String fileIdAndUserId : fileIdAndUserIdsArray) {
            String[] itemArray = fileIdAndUserId.split("_");
            fileInfoService.delFileBatch(itemArray[0],itemArray[1]);
        }
        return Result.success();
    }
}
