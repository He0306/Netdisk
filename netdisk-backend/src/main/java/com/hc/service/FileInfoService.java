package com.hc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.hc.entity.FileInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.UploadResultDto;
import com.hc.entity.query.FileInfoQuery;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author: 何超
 * @date: 2023-06-12 22:31
 * @description:
 */
public interface FileInfoService extends IService<FileInfo> {

    /**
     * 分页查询FileInfo全部数据
     *
     * @param query
     * @return
     */
    IPage<FileInfo> findFileInfoListByPage(FileInfoQuery query);

    /**
     * 分片上传
     *
     * @param sessionWebUserDto
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     */
    UploadResultDto uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    /**
     * 新建文件夹
     *
     * @param filePid
     * @param userId
     * @param folderName
     * @return
     */
    FileInfo newFolder(String filePid, String userId, String folderName);

    /**
     * 重命名
     *
     * @param fileId
     * @param userId
     * @param fileName
     * @return
     */
    FileInfo rename(String fileId, String userId, String fileName);

    /**
     * 获取所有目录
     *
     * @param filePid
     * @param currentFileIds
     * @param userId
     * @return
     */
    List<FileInfo> loadAllFolder(String filePid, String currentFileIds, String userId);

    /**
     * 移动目录
     *
     * @param fileIds
     * @param filePid
     * @param userId
     */
    void changeFileFolder(String fileIds, String filePid, String userId);

    /**
     * 创建下载链接
     *
     * @param fileId
     * @param userId
     * @return
     */
    String createDownLoadUrl(String fileId, String userId);

    /**
     * 下载
     *
     * @param request
     * @param response
     * @param code
     */
    void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException;

    /**
     * 单删或批量删除
     *
     * @param userId
     * @param fileIds
     */
    void removeFile2RecycleBatch(String userId, String fileIds);

    /**
     * 分页查询FileInfo 回收站 全部数据
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    IPage<FileInfo> findRecycleListByPage(String userId, Integer pageNum, Integer pageSize);

    /**
     * 还原或批量还原
     *
     * @param userId
     * @param fileIds
     */
    void recoverFileBatch(String userId, String fileIds);

    /**
     * 彻底删除
     *
     * @param userId
     * @param fileIds
     */
    void delFileBatch(String userId, String fileIds, Boolean isAdmin);

    /**
     * 管理员查看所以文件
     *
     * @param query
     * @return
     */
    PageInfo<FileInfo> findAdminFileInfoListByPage(FileInfoQuery query);

    /**
     * 校验文件夹
     *
     * @param rootFilePid
     * @param userId
     * @param fileId
     */
    void checkRootFilePid(String rootFilePid, String userId, String fileId);
}
