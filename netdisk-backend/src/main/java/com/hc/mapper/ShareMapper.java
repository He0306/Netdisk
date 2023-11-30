package com.hc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.entity.Share;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
@Mapper
public interface ShareMapper extends BaseMapper<Share> {

    /**
     * 单删除或批量删除分享
     *
     * @param shareIdArray
     * @param userId
     * @return
     */
    Integer deleteFileShareBatch(@Param("shareIdArray") String[] shareIdArray, @Param("userId") String userId);

    /**
     * 更加用户ID删除分享
     *
     * @param userId
     */
    void deleteByUserId(@Param("userId") String userId);

    /**
     * 更新浏览次数
     *
     * @param shareId
     */
    void updateShareShowCount(@Param("shareId") String shareId);
}
