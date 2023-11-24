package com.hc.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hc.common.enums.FileCategoryEnum;
import com.hc.common.lang.Constants;
import com.hc.entity.FileInfo;
import com.hc.service.FileInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author: hec
 * @date: 2023-09-20
 * @email: 2740860037@qq.com
 * @description: 文件预览工具类
 */
@Component
public class PreviewUtils {

    @Value("${project.folder}")
    private String projectFolder;

    @Autowired
    FileInfoService fileInfoService;

    /**
     * 图片预览
     *
     * @param response    servlet
     * @param imageFolder 文件路径
     * @param imageName   文件名称
     */
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (imageFolder == null || imageFolder.equals("") || imageName == null || imageName.equals("")) {
            return;
        }
        //取文件类型
        String suffix = imageName.substring(imageName.lastIndexOf(".") + 1);
        //获取文件路径
        String filePath = projectFolder + Constants.FILE_FOLDER_FILE + "/" + imageFolder + "/" + imageName;
        String contentType = "image/" + suffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, filePath);
    }

    /**
     * 文件预览
     *
     * @param response
     * @param fileId
     * @param userId
     */
    public void getFile(HttpServletResponse response, String fileId, String userId) {
        if (fileId == null || fileId.equals("")) {
            return;
        }
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId).eq(FileInfo::getFileId, fileId);
        FileInfo fileInfo = fileInfoService.getOne(wrapper);
        String filePath = fileInfo.getFilePath();
        // 源文件路径
        String realTargetFile = projectFolder + Constants.FILE_FOLDER_FILE + "/" + filePath;
        File file = new File(realTargetFile);
        if (!file.exists()){
            return;
        }
        readFile(response,realTargetFile);
    }

    /**
     * 视频预览
     *
     * @param response
     * @param fileId
     * @param userId
     */
    public void getVideo(HttpServletResponse response, String fileId, String userId) {

        String filePath = null;
        if (fileId.endsWith(".ts")) {
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileInfo::getFileId, realFileId);
            wrapper.eq(FileInfo::getUserId, userId);
            FileInfo fileInfo = fileInfoService.getOne(wrapper);
            if (null == fileInfo) {
                return;
            }
            String fileName = fileInfo.getFilePath();
            fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
            filePath = projectFolder + Constants.FILE_FOLDER_FILE + fileName;
        } else {
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileInfo::getFileId, fileId);
            wrapper.eq(FileInfo::getUserId, userId);
            FileInfo fileInfo = fileInfoService.getOne(wrapper);
            if (null == fileInfo) {
                return;
            }
            if (FileCategoryEnum.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = projectFolder + Constants.FILE_FOLDER_FILE + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
            } else {
                filePath = projectFolder + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
        }
        readFile(response, filePath);
    }

    /**
     * 获取目录信息
     *
     * @param path
     * @param userId
     * @return
     */
    public List<FileInfo> getFolderInfo(String path, String userId) {
        String[] pathArray = path.split("/");
        LambdaQueryWrapper<FileInfo> fileQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(userId)){
            fileQueryWrapper.eq(FileInfo::getUserId, userId);
        }
        fileQueryWrapper.in(FileInfo::getFileId, pathArray);
        fileQueryWrapper.select(FileInfo::getFileName, FileInfo::getFileId);
        return fileInfoService.list(fileQueryWrapper);
    }

    //让response读取对应的文件,这样response就会携带文件
    //就是简单的io操作
    public static void readFile(HttpServletResponse response, String filePath) {
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
