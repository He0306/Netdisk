package com.hc.entity.query;

import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-12 22:47
 * @description:
 */
@Data
public class FileInfoQuery extends BaseQuery{

    private Integer fileCategory;

    private Integer delFlag;

    private String fileId;

    private String filePid;

    private String nickName;

    private String fileIdFuzzy;

    private Integer folderType;

    private String[] fileArray;

    private String userId;

    private boolean queryExpire;

    private String userIdFuzzy;

    private String fileMd5;

    private String fileMd5Fuzzy;

    private String fileSize;

    private String fileName;

    private String fileNameFuzzy;

    private String fileCover;

    private String fileCoverFuzz;

    private String filePath;

    private String filePathFuzzy;

    private String createTime;

    private String createTimeStart;

    private Integer status;

}
