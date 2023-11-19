package com.hc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: 何超
 * @date: 2023-06-07 17:32
 * @description:
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    UserInfo selectByEmail(String email);

    Integer updateUserSpaceInteger(@Param("userId") String userId,@Param("userSpace")Long userSpace,@Param("totalSpace")Long totalSpace);

    Long selectUserSpace(String userId);
}
