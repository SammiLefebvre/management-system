package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工单（核心表）
 */
@Data
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号: YYYYMMDD/工单类型-0001 */
    private String workOrderCode;

    private Long deviceId;
    private String faultType;
    private String emergencyLevel;
    private String workOrderType;
    /** 参照物照片 URL */
    private String referencePhoto;

    private Long publisherId;
    private LocalDateTime publishTime;

    /** 状态: draft/published/claimed/in_progress/completing/confirmed/pending_force_close/closed */
    private String status;

    private Long claimerId;
    private LocalDateTime claimTime;
    private LocalDateTime startTime;
    private LocalDateTime inProgressTime;
    private LocalDateTime completeTime;
    private LocalDateTime confirmTime;

    /** 响应时长（分钟） */
    private Integer responseDuration;
    /** 修复时长（分钟） */
    private Integer repairDuration;

    /** 签到信息 */
    private BigDecimal checkinLat;
    private BigDecimal checkinLng;
    private LocalDateTime checkinTime;
    /** 签到照片 URL JSON 数组 */
    private String checkinPhotos;

    /** 排查过程文字 */
    private String processDesc;
    /** 排查照片 URL JSON 数组 */
    private String processPhotos;

    /** 结束照片 URL JSON 数组（含水印） */
    private String endPhotos;
    /** 维修结果: fixed/not_fixed */
    private String repairResult;
    /** 故障现象描述 */
    private String faultDescription;
    /** 特殊要求 */
    private String specialRequirements;
    /** 更换部件 */
    private String replacedParts;
    /** 维修人信息（JSON 班组信息） */
    private String repairerInfo;

    /** 是否置顶 */
    private Integer isPriority;

    /** 强制关闭 */
    private String forcedCloseReason;
    private Long forcedCloseBy;
    private LocalDateTime forcedCloseTime;
    private Long forcedConfirmBy;
    private LocalDateTime forcedConfirmTime;

    /** 项目组（数据隔离） */
    private String projectGroup;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
