package com.hc.entity.vo;

import com.hc.entity.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: hec
 * @date: 2023-10-28
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoPageVO extends BasePage{

    private List<UserInfo> list;
}
