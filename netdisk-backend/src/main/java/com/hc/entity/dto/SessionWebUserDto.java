package com.hc.entity.dto;

import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-09 15:36
 * @description:
 */
@Data
public class SessionWebUserDto {

    private String nickName;

    private String userId;

    private Boolean isAdmin;

    private String avatar;
}
