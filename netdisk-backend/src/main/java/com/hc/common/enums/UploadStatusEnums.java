package com.hc.common.enums;

/**
 * @author: hec
 * @date: 2023-09-12
 * @email: 2740860037@qq.com
 * @description: 上传枚举
 */
public enum UploadStatusEnums {
    UPLOAD_SECONDS("upload_seconds","妙传"),
    UPLOADING("uploading","上传中"),
    UPLOAD_FINISH("upload_finish","上传完成");

    private String code;

    private String desc;

    UploadStatusEnums(String code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
