package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 班组车辆
 */
@Data
@TableName("team_vehicle")
public class TeamVehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;
    private String vehicleName;
    private LocalDate date;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
