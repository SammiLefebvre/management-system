package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 码表
 */
@Data
@TableName("code_table")
public class CodeTable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 码表类型: emergency_level / project_group / fault_type / work_order_type */
    private String codeType;
    private String codeValue;
    private String codeLabel;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
