package com.hc.entity.vo;

import com.hc.entity.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: hec
 * @date: 2023-09-17
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoPageVO extends BasePage{

    private List<FileInfo> list;

}
