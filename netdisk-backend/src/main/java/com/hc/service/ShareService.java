package com.hc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.entity.Share;
import com.hc.entity.dto.SessionShareDto;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
public interface ShareService extends IService<Share> {

    /**
     * 分页查询全部分享
     *
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<Share> findShareListByPage(String userId, Integer pageNo, Integer pageSize);

    /**
     * 新增分享
     *
     * @param share
     */
    void saveShare(Share share);

    /**
     * 单删除或批量删除分享
     *
     * @param shareIdArray
     * @param userId
     */
    void deleteFileShareBatch(String[] shareIdArray, String userId);

    /**
     * 校验验分享证码
     *
     * @param shareId
     * @param code
     * @return
     */
    SessionShareDto checkShareCode(String shareId, String code);
}
