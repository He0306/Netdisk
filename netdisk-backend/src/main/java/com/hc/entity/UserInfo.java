package com.hc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "用户信息")
public class UserInfo {

    @ApiModelProperty(value = "用户ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "邮箱号")
    private String email;

    @ApiModelProperty(value = "qqID")
    private String qqOpenId;

    @ApiModelProperty(value = "qq头像")
    private String qqAvatar;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "使用空间")
    private Long userSpace;

    @ApiModelProperty(value = "总空间")
    private Long totalSpace;

    @ApiModelProperty(value = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;

    @ApiModelProperty(value = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    @ApiModelProperty(value = "ip地址")
    private String ipAddress;

    @ApiModelProperty(value = "来源")
    private String ipSource;

    @ApiModelProperty(value = "分片大小")
    private Integer chunkSize;


}
