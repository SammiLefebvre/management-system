package edu.cdut.aiback.dto;

import lombok.Data;

@Data
public class ReportQueryDTO {
    private String dataType; // work_order / device / personnel
    private String startDate;
    private String endDate;
    private String projectGroup;
    private String status;
}
