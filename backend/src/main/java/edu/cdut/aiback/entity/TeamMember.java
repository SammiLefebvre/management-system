package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 班组成员
 */
@Data
@TableName("team_member")
public class TeamMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;
    private Long personnelId;
    /** 是否司机 */
    private Integer isDriver;
    /** 排班起始日期 */
    private LocalDate date;

    /** 成员姓名（非数据库字段） */
    @TableField(exist = false)
    private String personnelName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
