package com.hc.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "邮箱验证码")
public class EmailCode {

    @ApiModelProperty(value = "邮箱号")
    private String email;

    @ApiModelProperty(value = "验证码")
    private String code;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
