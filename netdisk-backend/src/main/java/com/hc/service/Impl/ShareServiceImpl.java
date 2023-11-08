package com.hc.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.enums.ShareValidTypeEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.entity.FileInfo;
import com.hc.entity.Share;
import com.hc.mapper.FileInfoMapper;
import com.hc.mapper.ShareMapper;
import com.hc.service.ShareService;
import com.hc.utils.DateUtils;
import com.hc.utils.RandomNumberUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    @Autowired
    ShareMapper shareMapper;

    @Autowired
    FileInfoMapper fileInfoMapper;

    /**
     * 分页查询全部分享
     *
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Share> findShareListByPage(String userId, Integer pageNo, Integer pageSize) {
        Page<Share> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Share::getShareTime);
        Page<Share> sharePage = shareMapper.selectPage(page, wrapper);
        for (Share share : sharePage.getRecords()) {
            LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getFileId, share.getFileId());
            FileInfo fileInfo = fileInfoMapper.selectOne(queryWrapper);
            share.setFileName(fileInfo.getFileName());
            share.setStatus(fileInfo.getStatus());
            share.setFileCover(fileInfo.getFileCover());
            share.setFileType(fileInfo.getFileType());
            share.setFolderType(fileInfo.getFolderType());
        }
        return sharePage;
    }

    /**
     * 新增分享
     *
     * @param share
     */
    @Override
    public void saveShare(Share share) {
        ShareValidTypeEnum typeEnum = ShareValidTypeEnum.getByType(share.getValidType());
        if (null == typeEnum) {
            throw new ServiceException(HttpCodeEnum.CODE_600);
        }
        if (ShareValidTypeEnum.FOREVER != typeEnum) {
            share.setExpireTime(DateUtils.getAfterDate(typeEnum.getDays()));
        }
        Date curDate = new Date();
        share.setShareTime(curDate);
        if (StringUtils.isEmpty(share.getCode())) {
            share.setCode(RandomNumberUtil.getRandomString(Constants.LENGTH_5));
        }
        share.setUserId(share.getUserId());
        share.setShowCount(Constants.ZERO);
        shareMapper.insert(share);
    }

    /**
     * 单删除或批量删除分享
     *
     * @param shareIdArray
     * @param userId
     */
    @Override
    public void deleteFileShareBatch(String[] shareIdArray, String userId) {
        Integer count = shareMapper.deleteFileShareBatch(shareIdArray, userId);
        if (count != shareIdArray.length){
            throw new ServiceException(HttpCodeEnum.CODE_600);
        }
    }
}
