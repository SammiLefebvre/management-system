package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class WorkOrderExportRow {
    @ExcelProperty("工单编号")
    private String workOrderCode;
    @ExcelProperty("设备编号")
    private String deviceCode;
    @ExcelProperty("故障类型")
    private String faultType;
    @ExcelProperty("紧急程度")
    private String emergencyLevel;
    @ExcelProperty("状态")
    private String status;
    @ExcelProperty("发布时间")
    private String publishTime;
    @ExcelProperty("完成时间")
    private String completeTime;
}
