package edu.cdut.aiback.dto;

import lombok.Data;
import java.util.List;

/**
 * 作业流程提交请求
 */
@Data
public class WorkOrderProcessDTO {

    // ===== 签到 =====
    private String checkinLat;
    private String checkinLng;
    private List<String> checkinPhotos;

    // ===== 排查 =====
    private String processDesc;
    private List<String> processPhotos;

    // ===== 完工 =====
    private List<String> endPhotos;
    private String repairResult;       // fixed / not_fixed
    private String faultDescription;
    private String specialRequirements;
    private String replacedParts;
    private String repairerInfo;       // JSON 班组信息
}
