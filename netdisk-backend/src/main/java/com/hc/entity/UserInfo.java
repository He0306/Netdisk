package com.hc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: 何超
 * @date: 2023-06-07 17:31
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    private String nickName;

    private String email;

    private String qqOpenId;

    private String qqAvatar;

    private String password;

    private Integer status;

    private Long userSpace;

    private Long totalSpace;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    private String ipAddress;

    private String ipSource;

    private Integer chunkSize;


}
