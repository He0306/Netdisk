package com.hc.common.enums;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author: hec
 * @date: 2023-09-16
 * @email: 2740860037@qq.com
 * @description: 文件类型枚举
 */
public enum FileTypeEnum {

    VIDEO(FileCategoryEnum.VIDEO, 1, new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"}, "视频"),
    MUSIC(FileCategoryEnum.MUSIC, 2, new String[]{".mp3", ".wav", ".wma", ".mp2", ".flac", ".midi", ".ra", ".ape", ".aac", ".cda"}, "音频"),
    IMAGE(FileCategoryEnum.IMAGE, 3, new String[]{".jpeg",".jpg",".png",".gif",".bmp",".dds",".psd",".pdt",".webp",".xmp",".svg",".tiff"}, "图片"),
    PDF(FileCategoryEnum.DOC, 4, new String[]{".pdf"}, "pdf"),
    WORD(FileCategoryEnum.DOC, 5, new String[]{".doc", ".docx"}, "word"), EXCEL(FileCategoryEnum.DOC, 6, new String[]{".xls", ".xlsx"}, "excel"),
    TXT(FileCategoryEnum.DOC, 7, new String[]{".txt"}, "txt文本"),
    PROGRAME(FileCategoryEnum.OTHERS, 8, new String[]{".h", ".c", ".hpp", ".cpp", ".cc", ".c++", ".cxx", ".m", ".o", ".s", ".dll", ".cs", ".java", ".class", ".js", ".ts", ".css", ".scss", ".vue", ".jsx", ".sql", ".md", ".json", ".html", ".xml", ".jsp"}, "CODE"),
    ZIP(FileCategoryEnum.OTHERS, 9, new String[]{".rar", ".zip", ".7z", ".cab", ".arj", ".lzh", ".tar", ".gz", ".ace", ".uue", ".bz", ".jar", ".iso", ".mqp"}, "压缩包"),
    OTHERS(FileCategoryEnum.OTHERS, 10, new String[]{}, "其他");

    private FileCategoryEnum categoryEnum;

    private Integer type;

    private String[] suffix;

    private String desc;

    FileTypeEnum(FileCategoryEnum categoryEnum, Integer type, String[] suffix, String desc) {
        this.categoryEnum = categoryEnum;
        this.type = type;
        this.suffix = suffix;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (ArrayUtils.contains(item.getSuffix(), suffix)) {
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public FileCategoryEnum getCategoryEnum() {
        return categoryEnum;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public String getDesc() {
        return desc;
    }
}
