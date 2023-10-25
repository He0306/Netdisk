package com.hc.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: hec
 * @date: 2023-10-05
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownLoadFileDto {

    private String downloadCode;

    private String fileId;

    private String fileName;

    private String filePath;
}
