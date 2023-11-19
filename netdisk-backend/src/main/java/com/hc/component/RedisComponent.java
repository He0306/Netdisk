package com.hc.component;

import com.hc.common.lang.Constants;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.DownLoadFileDto;
import com.hc.entity.dto.SysSettingsDto;
import com.hc.entity.dto.UserSpaceDto;
import com.hc.mapper.FileInfoMapper;
import com.hc.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: 何超
 * @date: 2023-06-07 22:42
 * @description:
 */
@Component
public class RedisComponent {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Autowired
    UserInfoMapper userInfoMapper;

    /**
     * 获取用户设置信息
     *
     * @return
     */
    public SysSettingsDto sysSettingsDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtil.get(Constants.REDIS_KEY_SYS_SETTING);
        if (null == sysSettingsDto) {
            sysSettingsDto = new SysSettingsDto();
            redisUtil.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    /**
     * 保存系统设置信息
     *
     * @param sysSettingsDto
     */
    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        redisUtil.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }

    /**
     * 保存用户空间
     *
     * @param userId
     * @param userSpaceDto
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtil.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

    /**
     * 删除redis
     *
     * @param userId
     */
    public void deleteKey(String userId) {
        redisUtil.delKey(Constants.REDIS_KEY_USER_SPACE_USE + userId);
    }

    /**
     * 根据用户ID重置用户已使用的空间
     *
     * @param userId
     * @return
     */
    public UserSpaceDto restUserSpaceUse(String userId) {
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long useSpace = fileInfoMapper.selectUserSpace(userId);
        userSpaceDto.setUseSpace(useSpace);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisUtil.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
        return userSpaceDto;
    }

    /**
     * 获取用户空间
     *
     * @param userId
     * @return
     */
    public UserSpaceDto getUserSpaceDto(String userId) {
        UserSpaceDto spaceDto = (UserSpaceDto) redisUtil.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (spaceDto == null) {
            spaceDto = new UserSpaceDto();
            //  查询当前用户上传文件大小总和
            spaceDto.setUseSpace(userInfoMapper.selectUserSpace(userId));
            spaceDto.setTotalSpace(sysSettingsDto().getUserInitUserSpace() * Constants.MB);
            saveUserSpaceUse(userId, spaceDto);
        }
        return spaceDto;
    }

    /**
     * 保存临时大小
     *
     * @param userId
     * @param fileId
     * @param fileSize
     */
    public void saveFileTempSize(String userId, String fileId, Long fileSize) {
        Long currentSize = getFileTempSize(userId, fileId);
        redisUtil.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId, currentSize + fileSize, Constants.REDIS_KEY_EXPIRES_HOUR);
    }

    /**
     * 获取临时文件大小
     *
     * @param userId
     * @param fileId
     * @return
     */
    public Long getFileTempSize(String userId, String fileId) {
        return getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
    }

    private Long getFileSizeFromRedis(String key) {
        Object sizeObj = redisUtil.get(key);
        if (sizeObj == null) {
            return 0L;
        }
        if (sizeObj instanceof Integer) {
            return ((Integer) sizeObj).longValue();
        } else if (sizeObj instanceof Long) {
            return (Long) sizeObj;
        }
        return 0L;
    }

    /**
     * 保存下载信息
     *
     * @param code
     * @param dto
     */
    public void saveDownloadCode(String code, DownLoadFileDto dto) {
        redisUtil.setex(Constants.REDIS_KEY_DOWNLOAD + code, dto, Constants.REDIS_KEY_EXPIRES_HALF_AN_HOUR);
    }

    /**
     * 获取下载信息
     *
     * @param code
     * @return
     */
    public DownLoadFileDto getDownloadCode(String code) {
        return (DownLoadFileDto) redisUtil.get(Constants.REDIS_KEY_DOWNLOAD + code);
    }
}
