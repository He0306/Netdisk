package com.hc.task;

import com.hc.common.enums.FileDelFlagEnum;
import com.hc.entity.FileInfo;
import com.hc.entity.query.FileInfoQuery;
import com.hc.mapper.FileInfoMapper;
import com.hc.service.FileInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: hec
 * @date: 2023-12-02
 * @email: 2740860037@qq.com
 * @description: 定时清理回收站
 */
@Component
public class FileCleanTask {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanTask.class);

    @Autowired
    FileInfoMapper fileInfoMapper;

    @Autowired
    FileInfoService fileInfoService;

    /**
     * 定时删除在回收站10天的数据
     */
    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void execute() {
        logger.info("定时任务执行");
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        fileInfoQuery.setQueryExpire(true);
        List<FileInfo> fileInfoList = fileInfoMapper.findListByRecycle(fileInfoQuery);
        Map<String, List<FileInfo>> fileInfoMap = fileInfoList.stream()
                .collect(Collectors.groupingBy(FileInfo::getUserId));
        for (Map.Entry<String, List<FileInfo>> entry : fileInfoMap.entrySet()) {
            List<String> fileIds = entry.getValue().stream().map(p -> p.getFileId()).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(), String.join(",", fileIds), false);
        }
    }
}
