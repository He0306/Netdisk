package com.hc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.enums.FileCategoryEnum;
import com.hc.common.enums.FileDelFlagEnum;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.entity.FileInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.UploadResultDto;
import com.hc.entity.query.FileInfoQuery;
import com.hc.entity.vo.FileInfoPageVO;
import com.hc.service.FileInfoService;
import com.hc.utils.PreviewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author: 何超
 * @date: 2023-06-12 22:44
 * @description:
 */
@RestController
@RequestMapping("/file")
public class FileInfoController {

    @Autowired
    FileInfoService fileInfoService;

    @Autowired
    PreviewUtils previewUtils;

    /**
     * 分页查询全部文件
     *
     * @param session
     * @param query
     * @param category
     * @return
     */
    @PostMapping("/loadDataList")
    @GlobalInterceptor
    public Result loadDataList(HttpSession session, FileInfoQuery query, String category) {
        FileCategoryEnum categoryEnum = FileCategoryEnum.getByCode(category);
        if (null != categoryEnum) {
            query.setFileCategory(categoryEnum.getCategory());
        }
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        query.setUserId(sessionWebUserDto.getUserId());
        query.setDelFlag(FileDelFlagEnum.USING.getFlag());
        IPage<FileInfo> fileInfoIPage = fileInfoService.findFileInfoListByPage(query);
        FileInfoPageVO fileInfoPageVO = new FileInfoPageVO();
        fileInfoPageVO.setList(fileInfoIPage.getRecords());
        fileInfoPageVO.setPageNo(query.getPageNo());
        fileInfoPageVO.setPageSize(query.getPageSize());
        fileInfoPageVO.setPageTotal(fileInfoIPage.getCurrent());
        fileInfoPageVO.setTotalCount(fileInfoIPage.getTotal());
        return Result.success(fileInfoPageVO);
    }

    /**
     * 上传文件
     *
     * @param session
     * @param fileId
     * @param file
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @return
     */
    @PostMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public Result uploadFile(HttpSession session, String fileId, MultipartFile file, @VerifyParam(required = true) String fileName, @VerifyParam(required = true) String filePid, @VerifyParam(required = true) String fileMd5, @VerifyParam(required = true) Integer chunkIndex, @VerifyParam(required = true) Integer chunks) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        UploadResultDto resultDto = fileInfoService.uploadFile(sessionWebUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return Result.success(resultDto);
    }

    /**
     * 预览图片
     *
     * @param response
     * @param imageFolder
     * @param imageName
     */
    @GetMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor(checkParams = true)
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        previewUtils.getImage(response, imageFolder, imageName);
    }

    /**
     * 视频预览
     *
     * @param response
     * @param session
     * @param fileId
     */
    @GetMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getVideo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        previewUtils.getVideo(response, fileId, sessionWebUserDto.getUserId());
    }

    /**
     * 预览文件
     *
     * @param response
     * @param session
     * @param fileId
     */
    @RequestMapping("/getFile/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        previewUtils.getFile(response, fileId, sessionWebUserDto.getUserId());
    }

    /**
     * 新建文件夹
     *
     * @param session
     * @param filePid
     * @param fileName
     * @return
     */
    @PostMapping("/newFolder")
    @GlobalInterceptor(checkParams = true)
    public Result newFolder(HttpSession session, @VerifyParam(required = true) String filePid, @VerifyParam(required = true) String fileName) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        FileInfo fileInfo = fileInfoService.newFolder(filePid, sessionWebUserDto.getUserId(), fileName);
        return Result.success(fileInfo);
    }


    /**
     * 获取对应文件夹的信息
     *
     * @param session
     * @param path
     * @return
     */
    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public Result newFolder(HttpSession session, @VerifyParam(required = true) String path) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        List<FileInfo> folderInfo = previewUtils.getFolderInfo(path, sessionWebUserDto.getUserId());
        return Result.success(folderInfo);
    }

    /**
     * 重命名
     *
     * @param session
     * @param fileId
     * @param fileName
     * @return
     */
    @PostMapping("/rename")
    @GlobalInterceptor(checkParams = true)
    public Result rename(HttpSession session, @VerifyParam(required = true) String fileId, @VerifyParam(required = true) String fileName) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        FileInfo fileInfo = fileInfoService.rename(fileId, sessionWebUserDto.getUserId(), fileName);
        return Result.success(fileInfo);
    }

    /**
     * 获取所有目录
     *
     * @param session
     * @param filePid
     * @param currentFileIds
     * @return
     */
    @PostMapping("/loadAllFolder")
    @GlobalInterceptor(checkParams = true)
    public Result loadAllFolder(HttpSession session, @VerifyParam(required = true) String filePid, String currentFileIds) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        List<FileInfo> fileInfoList = fileInfoService.loadAllFolder(filePid, currentFileIds, sessionWebUserDto.getUserId());
        return Result.success(fileInfoList);
    }

    /**
     * 移动目录
     *
     * @param session
     * @param fileIds
     * @param filePid
     * @return
     */
    @PostMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true)
    public Result changeFileFolder(HttpSession session, @VerifyParam(required = true) String fileIds, @VerifyParam(required = true) String filePid) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        fileInfoService.changeFileFolder(fileIds, filePid, sessionWebUserDto.getUserId());
        return Result.success();
    }

    /**
     * 创建下载链接
     *
     * @param session
     * @param fileId
     * @return
     */
    @PostMapping("/createDownLoadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public Result createDownLoadUrl(HttpSession session, @VerifyParam(required = true) @PathVariable(value = "fileId") String fileId) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        String code = fileInfoService.createDownLoadUrl(fileId, sessionWebUserDto.getUserId());
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

    /**
     * 单删或批量删除
     *
     * @param session
     * @param fileIds
     * @return
     */
    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public Result delFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        fileInfoService.removeFile2RecycleBatch(sessionWebUserDto.getUserId(), fileIds);
        return Result.success();
    }
}
