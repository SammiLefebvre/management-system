package edu.cdut.aiback.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举
 */
@Getter
@AllArgsConstructor
public enum WorkOrderStatus {

    DRAFT("draft", "草稿"),
    PUBLISHED("published", "待认领"),
    CLAIMED("claimed", "进行中-待签到"),
    IN_PROGRESS("in_progress", "进行中-作业中"),
    COMPLETING("completing", "进行中-待完工"),
    PENDING_CONFIRM("pending_confirm", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    PENDING_FORCE_CLOSE("pending_force_close", "待关闭确认"),
    CLOSED("closed", "已关闭");

    private final String code;
    private final String label;

    public static WorkOrderStatus fromCode(String code) {
        for (WorkOrderStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知状态: " + code);
    }

    /**
     * 是否可发起强制关闭（任意非终态都可）
     */
    public boolean canForceClose() {
        return this != CLOSED && this != PENDING_FORCE_CLOSE && this != CONFIRMED;
    }
}
