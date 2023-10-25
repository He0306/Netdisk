package com.hc.entity.dto;

import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-14 14:44
 * @description: qq信息
 */
@Data
public class QQInfoDto {

    private Integer ret;

    private String msg;

    private String nickName;

    private String figureUrl_qq_1;

    private String figureUrl_qq_2;

    private String gender;
}
