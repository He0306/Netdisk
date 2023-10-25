package com.hc.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author: hec
 * @date: 2023-09-17
 * @email: 2740860037@qq.com
 * @description:
 */
public class ScaleFilter {

    private static final Logger logger = LoggerFactory.getLogger(ScaleFilter.class);

    /**
     * 生成视频封面
     * @param sourceFile
     * @param width
     * @param targetFile
     */
    public static void createCover4Video(File sourceFile, Integer width, File targetFile) {
        try {
            String cmd = "ffmpeg -i %s -y -frames:v 1 -vf scale=%d:%d/a %s";
            cmd = String.format(cmd, sourceFile.getAbsoluteFile(), width, width, targetFile.getAbsoluteFile());
            logger.info(cmd);
            ProcessUtils.executeCommand(cmd, false);
        } catch (Exception e) {
            logger.error("生成视频封面失败", e);
        }
    }

    public static Boolean createThumbnailWidthFFmpeg(File file, int thumbnailWidth, File targetFile, Boolean delSource) {
        try {
            BufferedImage src = ImageIO.read(file);
            int sorcew = src.getWidth();
            int sorceh = src.getHeight();
            if (sorcew <= thumbnailWidth) {
                return false;
            }
            compressImage(file, thumbnailWidth, targetFile, delSource);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 生成压缩图片
     * @param sourceFile
     * @param width
     * @param targetFile
     * @param delSource
     */
    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        try {
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsoluteFile(), width, targetFile.getAbsoluteFile()), false);
            if (delSource) {
                FileUtils.forceDelete(sourceFile);
            }
        } catch (Exception e) {
            logger.error("压缩图片失败");
        }
    }
}
