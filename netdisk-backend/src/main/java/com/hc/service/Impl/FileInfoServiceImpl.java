package com.hc.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hc.common.enums.*;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.component.RedisComponent;
import com.hc.entity.FileInfo;
import com.hc.entity.Share;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.DownLoadFileDto;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.dto.UploadResultDto;
import com.hc.entity.dto.UserSpaceDto;
import com.hc.entity.query.FileInfoQuery;
import com.hc.mapper.FileInfoMapper;
import com.hc.mapper.ShareMapper;
import com.hc.mapper.UserInfoMapper;
import com.hc.service.FileInfoService;
import com.hc.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: 何超
 * @date: 2023-06-12 22:31
 * @description:
 */
@Transactional
@Service(value = "fileInfoService")
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Autowired
    RedisComponent redisComponent;

    @Autowired
    UserInfoMapper userInfoMapper;

    @Value("${project.folder}")
    private String projectFolder;

    @Autowired
    @Lazy
    FileInfoServiceImpl fileInfoService;

    @Autowired
    ShareMapper shareMapper;


    /**
     * 分页查询FileInfo全部数据
     *
     * @param query
     * @return
     */
    @Override
    public IPage<FileInfo> findFileInfoListByPage(FileInfoQuery query) {
        Page<FileInfo> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        if (query.getFileCategory() != null) {
            wrapper.eq(FileInfo::getFileCategory, query.getFileCategory());
        }
        if (query.getFilePid() != null) {
            wrapper.eq(FileInfo::getFilePid, query.getFilePid());
        }
        if (!StringUtils.isEmpty(query.getFileNameFuzzy())) {
            wrapper.like(FileInfo::getFileName, query.getFileNameFuzzy());
        }
        if (!StringUtils.isEmpty(query.getUserId())) {
            wrapper.eq(FileInfo::getUserId, query.getUserId());
        }
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        wrapper.orderByDesc(FileInfo::getLastUpdateTime);
        return fileInfoMapper.selectPage(page, wrapper);
    }

    /**
     * 分页查询FileInfo 回收站 全部数据
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public IPage<FileInfo> findRecycleListByPage(String userId, Integer pageNum, Integer pageSize) {
        Page<FileInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId);
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.RECYCLE.getFlag());
        wrapper.orderByDesc(FileInfo::getRecoveryTime);
        return fileInfoMapper.selectPage(page, wrapper);
    }

    /**
     * 还原或批量还原
     *
     * @param userId
     * @param fileIds
     */
    @Override
    public void recoverFileBatch(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId);
        wrapper.in(FileInfo::getFileId, fileIdArray);
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.RECYCLE.getFlag());
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(wrapper);
        List<String> delFileSubFolderFileIdList = new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            if (FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) {
                findAllSubFolderFileList(delFileSubFolderFileIdList, userId, fileInfo.getFileId(), FileDelFlagEnum.RECYCLE.getFlag().toString());
            }
        }
        // 查询所有根目录的文件
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId, userId);
        queryWrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        queryWrapper.eq(FileInfo::getFilePid, Constants.ZERO_STR);
        List<FileInfo> selectedList = fileInfoMapper.selectList(queryWrapper);
        Map<String, FileInfo> rootFileMap = selectedList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) -> data2));
        // 查询所选文件 将目录下的所有删除的文件更新位使用中
        if (!delFileSubFolderFileIdList.isEmpty()) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
            fileInfoMapper.updateFileDelFlagBatch(fileInfo, null, delFileSubFolderFileIdList);
        }
        // 将选中的文件更新为使用中，且父级目录到根目录
        List<String> delFileIdList = Arrays.asList(fileIdArray);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
        fileInfo.setFilePid(Constants.ZERO_STR);
        fileInfo.setUserId(userId);
        fileInfo.setLastUpdateTime(new Date());
        fileInfoMapper.updateFileDelFlagBatch(fileInfo, delFileIdList, null);
        // 将所选文件重命名
        for (FileInfo item : fileInfoList) {
            FileInfo rootFileInfo = rootFileMap.get(item.getFileName());
            // 文件名已经存在，则重命名
            if (rootFileInfo != null) {
                String fileName = StringTools.rename(item.getFileName());
                FileInfo updateFileInfo = new FileInfo();
                updateFileInfo.setFileName(fileName);
                updateFileInfo.setFileId(item.getFileId());
                updateFileInfo.setUserId(userId);
                fileInfoMapper.updateByUserAndFileId(fileInfo);
            }
        }
    }

    /**
     * 管理员查看所以文件
     *
     * @param query
     * @return
     */
    @Override
    public PageInfo<FileInfo> findAdminFileInfoListByPage(FileInfoQuery query) {
        PageHelper.startPage(query.getPageNo().intValue(), query.getPageSize().intValue());
        List<FileInfo> list = fileInfoMapper.queryAdminList(query);
        return new PageInfo<>(list);
    }

    /**
     * 彻底删除
     *
     * @param userId
     * @param fileIds
     */
    @Override
    public void delFileBatch(String userId, String fileIds,Boolean isAdmin) {
        String[] fileIdArray = fileIds.split(",");
        LambdaQueryWrapper<Share> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Share::getUserId, userId).in(Share::getFileId, fileIdArray);
        Long count = shareMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(HttpCodeEnum.CODE_431);
        }
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId).in(FileInfo::getFileId, fileIdArray);
        if (isAdmin){
            wrapper.in(FileInfo::getDelFlag, FileDelFlagEnum.RECYCLE.getFlag(),FileDelFlagEnum.USING.getFlag());
        }else {
            wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.RECYCLE.getFlag());
        }
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(wrapper);

        List<String> delFileSubFileFolderFileIdList = new ArrayList<>();
        // 找到所选文件子目录文件ID
        for (FileInfo fileInfo : fileInfoList) {
            if (FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) {
                findAllSubFolderFileList(delFileSubFileFolderFileIdList, userId, fileInfo.getFileId(), "1,2");
            }
        }
        // 删除服务器文件
        for (FileInfo fileInfo : fileInfoList) {
            // 不删除秒传文件
            LambdaQueryWrapper<FileInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(FileInfo::getFileMd5,fileInfo.getFileMd5());
            Long selectCount = fileInfoMapper.selectCount(lambdaQueryWrapper);
            if (selectCount == 1) {
                new File(projectFolder + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath()).delete();
            }
            // 设置缓存
            UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userId);
            spaceDto.setUseSpace(spaceDto.getUseSpace() - fileInfo.getFileSize());
            redisComponent.saveUserSpaceUse(userId, spaceDto);
            // 更新用户使用空间
            UserInfo userInfo = userInfoMapper.selectById(userId);
            userInfo.setUserId(userId);
            userInfo.setUserSpace(userInfo.getUserSpace() - fileInfo.getFileSize());
            userInfoMapper.updateById(userInfo);
        }
        // 删除所有选中文件目录下的文件
        if (!delFileSubFileFolderFileIdList.isEmpty()) {
            fileInfoMapper.delFileBatch(userId, delFileSubFileFolderFileIdList, null);
        }
        // 删除文件
        fileInfoMapper.delFileBatch(userId, null, Arrays.asList(fileIdArray));
    }

    /**
     * 重命名
     *
     * @param fileId
     * @param userId
     * @param fileName
     * @return
     */
    @Override
    public FileInfo rename(String fileId, String userId, String fileName) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileId, fileId);
        wrapper.eq(FileInfo::getUserId, userId);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (null == fileInfo) {
            throw new ServiceException(HttpCodeEnum.CODE_421);
        }
        LambdaQueryWrapper<FileInfo> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FileInfo::getFileName, fileName).eq(FileInfo::getDelFlag,FileDelFlagEnum.USING.getFlag());
        Long count = fileInfoMapper.selectCount(countWrapper);
        if (count > 1) {
            throw new ServiceException(HttpCodeEnum.CODE_422);
        }
        String filePid = fileInfo.getFilePid();
        checkFileName(filePid, userId, fileName, fileInfo.getFolderType());
        // 获取文件后缀
        if (FileFolderTypeEnum.FILE.getType().equals(fileInfo.getFolderType())) {
            fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
        }
        Date curDate = new Date();
        FileInfo dbFileInfo = new FileInfo();
        dbFileInfo.setFileName(fileName);
        dbFileInfo.setLastUpdateTime(curDate);
        dbFileInfo.setFileId(fileId);
        dbFileInfo.setUserId(userId);
        // 修改文件名称
        fileInfoMapper.updateByFileIdAndUserId(dbFileInfo);
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(curDate);
        return fileInfo;
    }

    /**
     * 获取所有目录
     *
     * @param filePid
     * @param currentFileIds
     * @param userId
     * @return
     */
    @Override
    public List<FileInfo> loadAllFolder(String filePid, String currentFileIds, String userId) {
        String[] currentFileId = null;
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId);
        wrapper.eq(FileInfo::getFilePid, filePid);
        wrapper.eq(FileInfo::getFolderType, FileFolderTypeEnum.FOLDER.getType());
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        wrapper.orderByDesc(FileInfo::getCreateTime);
        if (!StringUtils.isEmpty(currentFileIds)) {
            currentFileId = currentFileIds.split(",");
            wrapper.notIn(FileInfo::getFileId, currentFileId);
        }
        return fileInfoMapper.selectList(wrapper);
    }

    /**
     * 创建下载链接
     *
     * @param fileId
     * @param userId
     * @return
     */
    @Override
    public String createDownLoadUrl(String fileId, String userId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileId, fileId).eq(FileInfo::getUserId, userId);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (fileInfo == null) {
            throw new ServiceException(HttpCodeEnum.CODE_600);
        }
        if (FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) {
            throw new ServiceException(HttpCodeEnum.CODE_600);
        }
        String code = RandomNumberUtil.getRandomString(Constants.LENGTH_50);
        DownLoadFileDto downLoadFileDto = new DownLoadFileDto();
        downLoadFileDto.setDownloadCode(code);
        downLoadFileDto.setFilePath(fileInfo.getFilePath());
        downLoadFileDto.setFileId(fileId);
        downLoadFileDto.setFileName(fileInfo.getFileName());

        // 存储redis 30分钟过期
        redisComponent.saveDownloadCode(code, downLoadFileDto);
        return code;
    }

    /**
     * 单删或批量删除
     *
     * @param userId
     * @param fileIds
     */
    @Override
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        LambdaQueryWrapper<Share> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Share::getUserId, userId).in(Share::getFileId, fileIdArray);
        Long count = shareMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(HttpCodeEnum.CODE_431);
        }
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId).in(FileInfo::getFileId, fileIdArray).eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(wrapper);
        if (fileInfoList == null || fileInfoList.size() == 0) {
            throw new ServiceException(HttpCodeEnum.CODE_426);
        }
        List<String> delFilePidList = new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            findAllSubFolderFileList(delFilePidList, userId, fileInfo.getFileId(), FileDelFlagEnum.USING.getFlag().toString());
        }
        // 将目录更新为删除
        if (!delFilePidList.isEmpty()) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUserId(userId);
            fileInfo.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
            fileInfoMapper.updateFileDelFlagBatch(fileInfo, null, delFilePidList);
        }
        // 将选中的文件更新为回收站
        List<String> delFileIdList = Arrays.asList(fileIdArray);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUserId(userId);
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        fileInfoMapper.updateFileDelFlagBatch(fileInfo, delFileIdList, null);
    }

    /**
     * 递归删除当前目录下的文件
     *
     * @param fileIdList
     * @param userId
     * @param fileId
     * @param delFlag
     */
    private void findAllSubFolderFileList(List<String> fileIdList, String userId, String fileId, String delFlag) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        String[] split = delFlag.split(",");
        wrapper.eq(FileInfo::getUserId, userId).eq(FileInfo::getFileId, fileId).in(FileInfo::getDelFlag, split).eq(FileInfo::getFolderType, FileFolderTypeEnum.FOLDER.getType());
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(wrapper);
        for (FileInfo fileInfo : fileInfoList) {
            findAllSubFolderFileList(fileIdList, userId, fileInfo.getFilePid(), delFlag);
            fileIdList.add(fileInfo.getFileId());
        }
    }

    /**
     * 下载
     *
     * @param request
     * @param response
     * @param code
     */
    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        DownLoadFileDto downLoadFileDto = redisComponent.getDownloadCode(code);
        if (null == downLoadFileDto) {
            throw new ServiceException(HttpCodeEnum.CODE_425);
        }
        String filePath = projectFolder + Constants.FILE_FOLDER_FILE + downLoadFileDto.getFilePath();
        String fileName = downLoadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("mise") > 0) {
            // IE浏览器,处理乱码问题，非重点
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        PreviewUtils.readFile(response, filePath);
    }

    /**
     * 移动目录
     *
     * @param fileIds
     * @param filePid
     * @param userId
     */
    @Override
    public void changeFileFolder(String fileIds, String filePid, String userId) {
        if (fileIds.equals(filePid)) {
            throw new ServiceException(HttpCodeEnum.CODE_423);
        }
        if (!Constants.ZERO_STR.equals(filePid)) {
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(FileInfo::getFileId, fileIds).eq(FileInfo::getUserId, userId);
            FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
            if (fileInfo == null || !FileDelFlagEnum.USING.getFlag().equals(fileInfo.getDelFlag())) {
                throw new ServiceException(HttpCodeEnum.CODE_424);
            }
        }
        String[] fileIdArray = fileIds.split(",");
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFilePid, filePid).eq(FileInfo::getUserId, userId);
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(wrapper);
        Map<String, FileInfo> dbFileNameMap = fileInfoList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) -> data2));
        // 查询选中的文件
        LambdaQueryWrapper<FileInfo> selectWrapper = new LambdaQueryWrapper<>();
        selectWrapper.in(FileInfo::getFileId, fileIdArray).eq(FileInfo::getUserId, userId);
        List<FileInfo> selectFileInfo = fileInfoMapper.selectList(selectWrapper);
        // 将所选文件重命名
        for (FileInfo item : selectFileInfo) {
            FileInfo rootFileInfo = dbFileNameMap.get(item.getFileName());
            // 文件名已经存在，重命名将还原的文件名
            FileInfo updateInfo = new FileInfo();
            if (rootFileInfo != null) {
                String fileName = StringTools.rename(item.getFileName());
                updateInfo.setFileName(fileName);
            }
            updateInfo.setFilePid(filePid);
            updateInfo.setUserId(userId);
            updateInfo.setFileId(item.getFileId());
            updateInfo.setLastUpdateTime(new Date());
            fileInfoMapper.updateByFileIdAndUserId(updateInfo);
        }
    }

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
    @Override
    public UploadResultDto uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
        UploadResultDto resultDto = new UploadResultDto();
        Boolean uploadSuccess = true;
        File tempFileFolder = null;
        try {
            if (StringUtils.isEmpty(fileId)) {
                fileId = RandomNumberUtil.getRandomString(Constants.LENGTH_10);
            }
            resultDto.setFileId(fileId);
            UserSpaceDto userSpaceDto = redisComponent.getUserSpaceDto(sessionWebUserDto.getUserId());
            if (chunkIndex == 0) {
                LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(FileInfo::getFileMd5, fileMd5);
                wrapper.eq(FileInfo::getStatus, FileStatusEnum.USING.getStatus());
                List<FileInfo> dbFileList = fileInfoMapper.selectList(wrapper);
                // 秒传
                if (!dbFileList.isEmpty()) {
                    FileInfo dbFile = dbFileList.get(0);
                    // 判断文件大小
                    if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                        throw new ServiceException(HttpCodeEnum.CODE_904);
                    }
                    dbFile.setFileId(fileId);
                    dbFile.setFilePid(filePid);
                    dbFile.setUserId(sessionWebUserDto.getUserId());
                    dbFile.setCreateTime(new Date());
                    dbFile.setLastUpdateTime(new Date());
                    dbFile.setStatus(FileStatusEnum.USING.getStatus());
                    dbFile.setDelFlag(FileDelFlagEnum.USING.getFlag());
                    dbFile.setFileMd5(fileMd5);
                    // 文件重命名
                    fileName = autoRename(filePid, sessionWebUserDto.getUserId(), fileName);
                    dbFile.setFileName(fileName);
                    fileInfoMapper.insert(dbFile);
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                    // 更新用户空间
                    updateUserSpace(sessionWebUserDto, dbFile.getFileSize());
                    return resultDto;
                }
            }
            // 判断磁盘空间
            Long currentTempSize = redisComponent.getFileTempSize(sessionWebUserDto.getUserId(), fileId);
            if (file.getSize() + currentTempSize + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                throw new ServiceException(HttpCodeEnum.CODE_904);
            }
            // 暂存临时目录
            String tempFolderName = projectFolder + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = sessionWebUserDto.getUserId() + fileId;

            tempFileFolder = new File(tempFolderName + currentUserFolderName);
            if (!tempFileFolder.exists()) {
                tempFileFolder.mkdirs();
            }
            File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
            file.transferTo(newFile);
            // 保存临时大小
            redisComponent.saveFileTempSize(sessionWebUserDto.getUserId(), fileId, file.getSize());
            if (chunkIndex < chunks - 1) {
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                return resultDto;
            }
            // 最后一个分片上传完成，记录数据库，异步合并分片
            String month = DateUtils.format(DateUtils.YYYYMM, new Date());
            String fileSuffix = StringTools.getFileSuffix(fileName);
            // 真实文件名
            String realFileName = currentUserFolderName + fileSuffix;
            FileTypeEnum fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            // 自动重命名
            fileName = autoRename(filePid, sessionWebUserDto.getUserId(), fileName);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            fileInfo.setUserId(sessionWebUserDto.getUserId());
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(month + "/" + realFileName);
            fileInfo.setFilePid(filePid);
            fileInfo.setCreateTime(new Date());
            fileInfo.setLastUpdateTime(new Date());
            fileInfo.setFileCategory(fileTypeEnum.getCategoryEnum().getCategory());
            fileInfo.setFileType(fileTypeEnum.getType());
            fileInfo.setStatus(FileStatusEnum.TRANSFER.getStatus());
            fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
            fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
            fileInfoMapper.insert(fileInfo);
            Long totalSize = redisComponent.getFileTempSize(sessionWebUserDto.getUserId(), fileId);
            updateUserSpace(sessionWebUserDto, totalSize);
            resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
            // 异步
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileInfoService.transferFile(fileInfo.getFileId(), sessionWebUserDto);
                }
            });

            return resultDto;
        } catch (ServiceException e) {
            logger.error("文件上传失败", e);
            uploadSuccess = false;
            throw e;
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            uploadSuccess = false;
        } finally {
            if (!uploadSuccess && tempFileFolder != null) {
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    logger.error("删除临时目录失败", e);
                }
            }
        }
        return resultDto;
    }

    /**
     * 新建文件夹
     *
     * @param filePid
     * @param userId
     * @param folderName
     * @return
     */
    @Override
    public FileInfo newFolder(String filePid, String userId, String folderName) {
        checkFileName(filePid, userId, folderName, FileFolderTypeEnum.FOLDER.getType());
        Date curDate = new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(RandomNumberUtil.getRandomString(Constants.LENGTH_10));
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(filePid);
        fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
        fileInfo.setCreateTime(curDate);
        fileInfo.setFileName(folderName);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setStatus(FileStatusEnum.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }


    /**
     * 检查同名目录是否已经存在同名文件名
     *
     * @param filePid
     * @param userId
     * @param fileName
     * @param folderType
     */
    private void checkFileName(String filePid, String userId, String fileName, Integer folderType) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFolderType, folderType);
        wrapper.eq(FileInfo::getFileName, fileName);
        wrapper.eq(FileInfo::getFilePid, filePid);
        wrapper.eq(FileInfo::getUserId, userId);
        wrapper.eq(FileInfo::getDelFlag,FileDelFlagEnum.USING.getFlag());
        Long count = fileInfoMapper.selectCount(wrapper);
        if (count > 0) {
            throw new ServiceException(HttpCodeEnum.CODE_420);
        }
    }

    /**
     * 异步
     *
     * @param fileId
     * @param sessionWebUserDto
     */
    @Async
    public void transferFile(String fileId, SessionWebUserDto sessionWebUserDto) {
        Boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnum fileTypeEnum = null;
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileId, fileId);
        wrapper.eq(FileInfo::getUserId, sessionWebUserDto.getUserId());
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        try {
            if (fileInfo == null || !FileStatusEnum.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
                return;
            }
            // 临时目录
            String tempFolderName = projectFolder + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = sessionWebUserDto.getUserId() + fileId;
            File fileFolder = new File(tempFolderName + currentUserFolderName);

            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
            String month = DateUtils.format(DateUtils.YYYYMM, fileInfo.getCreateTime());
            // 目标目录
            String targetFolderName = projectFolder + Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            // 真实的文件名
            String realFileName = currentUserFolderName + fileSuffix;
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            // 合并文件
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
            // 视频文件切割
            fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            if (FileTypeEnum.VIDEO == fileTypeEnum) {
                cutFile4Video(fileId, targetFilePath);
                // 生成视频缩略图
                cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + "/" + cover;
                ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
            } else if (FileTypeEnum.IMAGE == fileTypeEnum) {
                // 生成缩略图
                cover = month + "/" + realFileName.replace(".", "_.");
                String coverPath = targetFolderName + "/" + cover;
                Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
                logger.info("生成缩略图{}", created);
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            logger.error("文件转码失败，文件ID：{}，userId：{}", fileId, sessionWebUserDto.getUserId());
            transferSuccess = false;
        } finally {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setFileSize(new File(targetFilePath).length());
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess ? FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithholdStatus(fileId, sessionWebUserDto.getUserId(), updateInfo, FileStatusEnum.TRANSFER.getStatus());
        }
    }

    /**
     * 合并文件
     *
     * @param dirPath    临时目录
     * @param toFilePath 真实的文件目录
     * @param fileName   真实的文件名
     * @param delSource  是否关闭
     */
    private void union(String dirPath, String toFilePath, String fileName, Boolean delSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new ServiceException(HttpCodeEnum.CODE_416);
        }
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                File chunkFile = new File(dirPath + "/" + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    logger.error("合并分片失败", e);
                    throw new ServiceException(HttpCodeEnum.CODE_417);
                }
            }
        } catch (Exception e) {
            logger.error("合并文件：{}失败", fileName, e);
            throw new ServiceException(HttpCodeEnum.CODE_418);
        } finally {
            if (null != writeFile) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (delSource && dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件名  重命名
     *
     * @param filePid
     * @param userId
     * @param fileName
     * @return
     */
    private String autoRename(String filePid, String userId, String fileName) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId);
        wrapper.eq(FileInfo::getFilePid, filePid);
        wrapper.eq(FileInfo::getDelFlag, FileDelFlagEnum.USING.getFlag());
        wrapper.eq(FileInfo::getFileName, fileName);
        Long count = fileInfoMapper.selectCount(wrapper);
        if (count > 0) {
            fileName = StringTools.rename(fileName);
        }
        return fileName;
    }

    /**
     * 更新用户使用空间
     *
     * @param webUserDto
     * @param userSpace
     */
    private void updateUserSpace(SessionWebUserDto webUserDto, Long userSpace) {
        Integer count = userInfoMapper.updateUserSpaceInteger(webUserDto.getUserId(), userSpace, null);
        if (count == 0) {
            throw new ServiceException(HttpCodeEnum.CODE_904);
        }
        UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(webUserDto.getUserId());
        spaceDto.setUseSpace(spaceDto.getUseSpace() + userSpace);
        redisComponent.saveUserSpaceUse(webUserDto.getUserId(), spaceDto);
    }

    private void cutFile4Video(String fileId, String videoFilePath) {
        // 创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        // 生成.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        // 生成索引文件.m3u8和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        // 删除index.tx
        new File(tsPath).delete();
    }
}
