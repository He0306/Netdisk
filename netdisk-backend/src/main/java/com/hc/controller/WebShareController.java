package com.hc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.enums.FileDelFlagEnum;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.entity.FileInfo;
import com.hc.entity.Share;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SessionShareDto;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.vo.FileInfoPageVO;
import com.hc.entity.vo.ShareInfoVo;
import com.hc.service.FileInfoService;
import com.hc.service.ShareService;
import com.hc.service.UserInfoService;
import com.hc.utils.PreviewUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * @author: hec
 * @date: 2023-11-25
 * @email: 2740860037@qq.com
 * @description: 外部分享控制器
 */
@RequestMapping("/showShare")
@RestController
public class WebShareController {

    @Autowired
    ShareService shareService;

    @Autowired
    FileInfoService fileInfoService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    PreviewUtils previewUtils;

    /**
     * 获取登录用户信息
     *
     * @param session
     * @param shareId
     * @return
     */
    @PostMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result getShareLoginInfo(HttpSession session,
                                    @VerifyParam(required = true) String shareId) {
        SessionShareDto sessionShareDto = (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
        if (sessionShareDto == null) {
            return Result.success();
        }
        ShareInfoVo shareInfoVo = getShareInfoCommon(shareId);
        // 判断是否是当前用户分享的文件
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (sessionShareDto != null && sessionWebUserDto.getUserId().equals(sessionShareDto.getShareUserId())) {
            shareInfoVo.setCurrentUser(true);
        } else {
            shareInfoVo.setCurrentUser(false);
        }
        return Result.success(shareInfoVo);
    }

    /**
     * 根据分享ID查询分享信息
     *
     * @param shareId
     * @return
     */
    @PostMapping("/getShareInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result getShareInfo(@VerifyParam(required = true) String shareId) {
        return Result.success(getShareInfoCommon(shareId));
    }

    /**
     * 校验分享验证码
     *
     * @param session
     * @param shareId
     * @param code
     * @return
     */
    @PostMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result checkShareCode(HttpSession session,
                                 @VerifyParam(required = true) String shareId,
                                 @VerifyParam(required = true) String code) {
        SessionShareDto sessionShareDto = shareService.checkShareCode(shareId, code);
        session.setAttribute(Constants.SESSION_SHARE_KEY + shareId, sessionShareDto);
        return Result.success();
    }

    /**
     * 查询分享列表
     *
     * @param session
     * @param shareId
     * @param filePid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public Result loadFileList(HttpSession session,
                               @VerifyParam(required = true) String shareId,
                               String filePid,
                               Integer pageNo,
                               Integer pageSize) {
        SessionShareDto shareDto = checkShare(session, shareId);
        Page<FileInfo> infoPage = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)) {
            fileInfoService.checkRootFilePid(shareDto.getFileId(), shareDto.getShareUserId(), filePid);
            wrapper.eq(FileInfo::getFilePid, filePid);
        } else {
            wrapper.eq(FileInfo::getFileId, shareDto.getFileId());
        }
        wrapper.eq(FileInfo::getUserId, shareDto.getShareUserId());
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        wrapper.orderByDesc(FileInfo::getLastUpdateTime);
        Page<FileInfo> fileInfoIPage = fileInfoService.page(infoPage, wrapper);
        FileInfoPageVO fileInfoPageVO = new FileInfoPageVO();
        fileInfoPageVO.setList(fileInfoIPage.getRecords());
        fileInfoPageVO.setPageNo(Long.valueOf(pageNo));
        fileInfoPageVO.setPageSize(Long.valueOf(pageSize));
        fileInfoPageVO.setPageTotal(fileInfoIPage.getCurrent());
        fileInfoPageVO.setTotalCount(fileInfoIPage.getTotal());
        return Result.success(fileInfoPageVO);
    }

    /**
     * 保存到我的网盘
     *
     * @param session
     * @param shareId
     * @param shareFileIds
     * @param myFolderId
     * @return
     */
    @PostMapping("/saveShare")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public Result saveShare(HttpSession session,
                            @VerifyParam(required = true) String shareId,
                            @VerifyParam(required = true) String shareFileIds,
                            @VerifyParam(required = true) String myFolderId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (sessionShareDto.getShareUserId().equals(sessionWebUserDto.getUserId())) {
            throw new ServiceException(HttpCodeEnum.CODE_435);
        }
        fileInfoService.saveShare(sessionShareDto.getFileId(),shareFileIds,myFolderId,sessionShareDto.getShareUserId(),sessionWebUserDto.getUserId());
        return Result.success();
    }


    /**
     * 通用-查询分享信息
     *
     * @param shareId
     * @return
     */
    private ShareInfoVo getShareInfoCommon(String shareId) {
        Share share = shareService.getById(shareId);
        if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            throw new ServiceException(HttpCodeEnum.CODE_432);
        }
        ShareInfoVo shareInfoVo = new ShareInfoVo();
        BeanUtils.copyProperties(share, shareInfoVo);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileId, share.getFileId()).eq(FileInfo::getUserId, share.getUserId());
        FileInfo fileInfo = fileInfoService.getOne(wrapper);
        if (fileInfo == null || !FileDelFlagEnum.USING.getFlag().equals(fileInfo.getDelFlag())) {
            throw new ServiceException(HttpCodeEnum.CODE_432);
        }
        shareInfoVo.setFileName(fileInfo.getFileName());
        UserInfo userInfo = userInfoService.getById(share.getUserId());
        shareInfoVo.setNickName(userInfo.getNickName());
        shareInfoVo.setAvatar(userInfo.getQqAvatar());
        shareInfoVo.setUserId(userInfo.getUserId());
        return shareInfoVo;
    }

    /**
     * 预览文件
     *
     * @param response
     * @param session
     * @param shareId
     * @param fileId
     */
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @PathVariable("shareId") String shareId,
                        @PathVariable("fileId") String fileId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        previewUtils.getFile(response, fileId, sessionShareDto.getShareUserId());
    }

    /**
     * 创建下载链接
     *
     * @param session
     * @param shareId
     * @param fileId
     * @return
     */
    @PostMapping("/createDownLoadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public Result createDownLoadUrl(HttpSession session,
                                    @VerifyParam(required = true) @PathVariable(value = "shareId") String shareId,
                                    @VerifyParam(required = true) @PathVariable(value = "fileId") String fileId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        String code = fileInfoService.createDownLoadUrl(fileId, sessionShareDto.getShareUserId());
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
    public void download(HttpServletResponse response,
                         HttpServletRequest request,
                         @VerifyParam(required = true) @PathVariable(value = "code") String code) throws UnsupportedEncodingException {
        fileInfoService.download(request, response, code);
    }

    /**
     * 获取对应文件夹的信息
     *
     * @param session
     * @param path
     * @param shareId
     * @return
     */
    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public Result newFolder(HttpSession session,
                            @VerifyParam(required = true) String path,
                            @VerifyParam(required = true) String shareId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        List<FileInfo> folderInfo = previewUtils.getFolderInfo(path, sessionShareDto.getShareUserId());
        return Result.success(folderInfo);
    }

    /**
     * 验证分享码
     *
     * @param session
     * @param shareId
     * @return
     */
    private SessionShareDto checkShare(HttpSession session, String shareId) {
        SessionShareDto shareDto = (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
        if (null == shareDto) {
            throw new ServiceException(HttpCodeEnum.CODE_434);
        }
        if (shareDto.getExpireTime() != null && new Date().after(shareDto.getExpireTime())) {
            throw new ServiceException(HttpCodeEnum.CODE_432);
        }
        return shareDto;
    }
}