package edu.cdut.aiback.dto;

import lombok.Data;

/**
 * 人员管理 - 查询请求
 */
@Data
public class PersonnelQueryDTO {

    private String name;
    private String role;
    private String projectGroup;
    private Integer status;

    private Integer page = 1;
    private Integer size = 10;
}
