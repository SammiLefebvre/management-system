package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工单操作日志
 */
@Data
@TableName("work_order_log")
public class WorkOrderLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;
    private Long operatorId;
    private String operatorName;
    /** 操作类型: publish/claim/cancel_claim/start/in_progress/complete/confirm/force_close/confirm_force_close */
    private String action;
    private LocalDateTime actionTime;
    private String remark;
}
