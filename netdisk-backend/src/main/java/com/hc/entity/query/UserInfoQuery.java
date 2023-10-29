package com.hc.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: hec
 * @date: 2023-10-28
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoQuery extends BaseQuery{

    private String nickName;

    private Integer status;
}
