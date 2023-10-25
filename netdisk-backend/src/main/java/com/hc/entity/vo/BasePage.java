package com.hc.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasePage {

    private Long pageNo;

    private Long pageSize;

    private Long pageTotal;

    private Long totalCount;
}
