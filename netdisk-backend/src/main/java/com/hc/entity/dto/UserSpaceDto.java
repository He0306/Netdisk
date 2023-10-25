package com.hc.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: 何超
 * @date: 2023-06-09 15:47
 * @description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpaceDto implements Serializable {

    private Long useSpace;

    private Long totalSpace;
}
