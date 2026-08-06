package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class CandidateEngineerVO {
    private Long personnelId;
    private String name;
    private String phone;
    private Double distanceKm;
    private Integer pendingCount;
    private Double avgResponseMinutes;
    private Integer completedThisWeek;
}
