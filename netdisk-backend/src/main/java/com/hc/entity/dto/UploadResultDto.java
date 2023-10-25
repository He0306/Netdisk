package com.hc.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: 何超
 * @date: 2023-06-23 20:11
 * @description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResultDto implements Serializable {

    private String fileId;

    private String status;
}
