package edu.cdut.aiback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工单编号序列（用于行锁自增）
 */
@Data
@TableName("work_order_seq")
public class WorkOrderSeq {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日期前缀: YYYYMMDD/工单类型 */
    private String dateKey;

    /** 当前最大序号 */
    private Integer currentSeq;
}
