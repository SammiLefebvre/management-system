package edu.cdut.aiback.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    INTERNAL("内场", "内场人员"),
    EXTERNAL("外场", "外场工程师"),
    PROJECT_MANAGER("项目管理", "项目管理人员"),
    COMPANY_MANAGER("公司管理", "公司管理人员");

    private final String code;
    private final String label;

    /**
     * 是否拥有管理后台访问权限
     */
    public boolean canAccessAdmin() {
        return this != EXTERNAL;
    }

    /**
     * 是否可发起强制关闭
     */
    public boolean canForceClose() {
        return this == COMPANY_MANAGER || this == PROJECT_MANAGER;
    }

    /**
     * 是否可确认强制关闭
     */
    public boolean canConfirmForceClose() {
        return this == INTERNAL;
    }

    /**
     * 是否可发布/确认工单
     */
    public boolean canPublishOrConfirm() {
        return this == INTERNAL || this == PROJECT_MANAGER || this == COMPANY_MANAGER;
    }
}
