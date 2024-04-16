package com.hc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description: 分享实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "分享信息")
public class Share implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String shareId;

    @ApiModelProperty(value = "文件ID")
    private String fileId;

    @ApiModelProperty(value = "文件名称")
    @TableField(exist = false)
    private String fileName;

    @ApiModelProperty(value = "状态")
    @TableField(exist = false)
    private Integer status;

    @ApiModelProperty(value = "目录类型")
    @TableField(exist = false)
    private Integer folderType;

    @ApiModelProperty(value = "文件封面")
    @TableField(exist = false)
    private String fileCover;

    @ApiModelProperty(value = "文件类型")
    @TableField(exist = false)
    private Integer fileType;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "有效期")
    private Integer validType;

    @ApiModelProperty(value = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    @ApiModelProperty(value = "分享时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;

    @ApiModelProperty(value = "分享码")
    private String code;

    @ApiModelProperty(value = "查看次数")
    private Integer showCount;
}
