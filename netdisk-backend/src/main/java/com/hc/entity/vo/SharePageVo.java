package com.hc.entity.vo;

import com.hc.entity.Share;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharePageVo extends BasePage {

    private List<Share> list;
}
