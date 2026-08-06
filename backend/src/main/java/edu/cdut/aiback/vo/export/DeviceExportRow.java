package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DeviceExportRow {
    @ExcelProperty("设备编号") private String deviceCode;
    @ExcelProperty("设备名称") private String deviceName;
    @ExcelProperty("区域") private String area;
    @ExcelProperty("IP") private String ip;
    @ExcelProperty("项目组") private String projectGroup;
}
