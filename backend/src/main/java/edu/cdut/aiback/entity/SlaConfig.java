package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * SLA 配置
 */
@Data
@TableName("sla_config")
public class SlaConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String emergencyLevel;
    private Integer targetResponseMinutes;
    private Integer targetRepairMinutes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
