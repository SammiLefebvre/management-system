package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备台账
 */
@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceCode;
    private String deviceName;
    private String area;
    private String mac;
    private String ip;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String cameraType;
    private String operationType;
    private String projectGroup;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
