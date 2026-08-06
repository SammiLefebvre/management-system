package edu.cdut.aiback.dto;

import lombok.Data;

/**
 * 设备台账 - 查询请求
 */
@Data
public class DeviceQueryDTO {

    private String deviceCode;
    private String deviceName;
    private String area;
    private String operationType;
    private String projectGroup;

    private Integer page = 1;
    private Integer size = 10;
}
