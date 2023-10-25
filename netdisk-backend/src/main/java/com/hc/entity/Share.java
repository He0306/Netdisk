package com.hc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Share implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String shareId;

    private String fileId;

    @TableField(exist = false)
    private String fileName;

    @TableField(exist = false)
    private Integer status;

    @TableField(exist = false)
    private Integer folderType;

    @TableField(exist = false)
    private String fileCover;

    @TableField(exist = false)
    private Integer fileType;

    private String userId;

    private Integer validType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;

    private String code;

    private Integer showCount;
}
