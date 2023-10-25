package com.hc.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author: 何超
 * @date: 2023-06-12 23:01
 * @description:
 */
@Data
public class FileInfoVo {

    private String fileId;

    private String filePid;

    private Long fileSize;

    private String fileName;

    private String fileCover;

    private Date lastUpdateTime;

    private Integer folderType;

    private Integer fileType;

    private Integer status;
}
