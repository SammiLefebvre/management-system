package edu.cdut.aiback.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 班组创建/编辑请求
 */
@Data
public class TeamDTO {

    private Long id;
    /** 组名 */
    private String teamName;
    /** 班组成员 ID 列表 */
    private List<Long> memberIds;
    /** 司机的人员 ID */
    private Long driverId;
    /** 车辆名称列表 */
    private List<String> vehicles;
    /** 排班起始日期 */
    private LocalDate date;
}
