package com.hc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: 何超
 * @date: 2023-06-07 20:13
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCode {

    private String email;

    private String code;

    private Integer status;

    private Date createTime;
}
