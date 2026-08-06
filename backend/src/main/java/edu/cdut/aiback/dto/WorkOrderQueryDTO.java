package edu.cdut.aiback.dto;

import lombok.Data;

/**
 * 工单列表查询请求
 */
@Data
public class WorkOrderQueryDTO {

    private String faultType;
    private String emergencyLevel;
    private Long claimerId;
    /** 状态，多选逗号分隔 */
    private String status;
    private String startDate;
    private String endDate;

    private Integer page = 1;
    private Integer size = 10;
}
