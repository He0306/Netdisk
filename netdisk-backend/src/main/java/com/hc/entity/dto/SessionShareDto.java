package com.hc.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: hec
 * @date: 2023-11-26
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionShareDto {

    private String shareId;

    private String shareUserId;

    private Date expireTime;

    private String fileId;
}
