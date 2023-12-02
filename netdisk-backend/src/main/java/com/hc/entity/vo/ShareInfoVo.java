package com.hc.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: hec
 * @date: 2023-11-25
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareInfoVo {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    private String nickName;

    private String fileName;

    private Boolean currentUser;

    private String fileId;

    private String avatar;

    private String userId;
}
