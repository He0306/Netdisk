package com.hc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.entity.FileInfo;
import com.hc.entity.query.FileInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author: 何超
 * @date: 2023-06-12 22:30
 * @description:
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    /**
     * 查询用户空间
     *
     * @param userId
     * @return
     */
    Long selectUserSpace(@Param("userId") String userId);

    /**
     * 修改文件状态
     *
     * @param fileId
     * @param userId
     * @param fileInfo
     * @param oldStatus
     */
    void updateFileStatusWithholdStatus(@Param("fileId") String fileId, @Param("userId") String userId, FileInfo fileInfo, @Param("oldStatus") Integer oldStatus);

    /**
     * 修改文件名称
     *
     * @param dbFileInfo
     */
    void updateByFileIdAndUserId(FileInfo dbFileInfo);

    /**
     * 更新fileIds 或者 filePids 批量更新
     *
     * @param fileInfo
     * @param fileIds
     * @param filePids
     */
    void updateFileDelFlagBatch(FileInfo fileInfo, List<String> fileIds, List<String> filePids);

    /**
     * 更加userId fileId进行更新
     *
     * @param fileInfo
     */
    void updateByUserAndFileId(FileInfo fileInfo);

    /**
     * 根据用户ID删除
     *
     * @param userId
     */
    void deleteByUserId(@Param("userId") String userId);

    /**
     * 管理员查询所有文件
     *
     * @param query
     * @return
     */
    List<FileInfo> queryAdminList(@Param("query") FileInfoQuery query);

    /**
     * 彻底删除
     *
     * @param userId
     * @param filePidList
     * @param fileIdList
     */
    void delFileBatch(@Param("userId") String userId, @Param("filePidList") List<String> filePidList, @Param("fileIdList") List<String> fileIdList);

    /**
     * 查询
     *
     * @param fileInfoQuery
     */
    List<FileInfo> findListByRecycle(FileInfoQuery fileInfoQuery);

    /**
     * 根据userId和filePids查询总文件大小
     *
     * @param userId
     * @param delFileSubFileFolderFileIdList
     * @return
     */
    Long selectByUserIdAndFilePids(@Param("userId") String userId, @Param("filePids") List<String> delFileSubFileFolderFileIdList);
}
