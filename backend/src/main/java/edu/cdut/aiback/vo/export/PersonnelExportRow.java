package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PersonnelExportRow {
    @ExcelProperty("账号") private String account;
    @ExcelProperty("姓名") private String name;
    @ExcelProperty("手机号") private String phone;
    @ExcelProperty("角色") private String role;
    @ExcelProperty("项目组") private String projectGroup;
    @ExcelProperty("状态") private String status;
}
