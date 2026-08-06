package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class PersonnelWorkloadVO {
    private Long personnelId;
    private String name;
    private String role;
    private Long pendingCount;
    private Long completedThisWeek;
    private Double avgResponseMinutes;
}
