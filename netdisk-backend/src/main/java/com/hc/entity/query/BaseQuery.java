package com.hc.entity.query;

import lombok.Data;

/**
 * @author: 何超
 * @date: 2023-06-23 00:20
 * @description:
 */
@Data
public class BaseQuery {

    private Long pageNo = 1L;

    private Long pageSize = 15L;
}
