package com.hc.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: 何超
 * @date: 2023-06-07 22:43
 * @description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto implements Serializable {

    private String registerEmailTitle = "云盘邮箱验证服务";

    private String registerEmailContent = "您好，您的邮箱验证码是：%s，5分钟有效";

    /**
     * 用户总空间
     */
    private Integer userInitUserSpace = 5;
}
