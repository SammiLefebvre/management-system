package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人员管理
 */
@Data
@TableName("personnel")
public class Personnel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String wxOpenId;
    private String account;
    private String name;
    private String phone;
    /** 角色: 内场/外场/项目管理/公司管理 */
    private String role;
    private String projectGroup;
    private Integer status;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @TableField(exist = false)
    private Integer pendingCount;

    @TableField(exist = false)
    private Double avgResponse;

    @TableField(exist = false)
    private Integer completedWeek;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
