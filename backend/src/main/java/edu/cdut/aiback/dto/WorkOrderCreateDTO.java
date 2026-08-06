package edu.cdut.aiback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单发布请求
 */
@Data
public class WorkOrderCreateDTO {

    /** 工单类型（从码表选择） */
    @NotBlank(message = "工单类型不能为空")
    private String workOrderType;

    /** 设备 ID */
    @NotNull(message = "故障点位不能为空")
    private Long deviceId;

    /** 故障描述 */
    private String faultDescription;

    /** 故障类型 */
    private String faultType;

    /** 紧急程度: 一级/二级/三级 */
    @NotBlank(message = "紧急程度不能为空")
    private String emergencyLevel;

    /** 参照物照片 URL */
    private String referencePhoto;

    /** 是否立即发布（false = 保存草稿） */
    private Boolean publishNow = true;
}
