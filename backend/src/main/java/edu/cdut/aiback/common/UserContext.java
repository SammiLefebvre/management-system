package edu.cdut.aiback.common;

import lombok.Data;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void set(UserInfo user) {
        USER_HOLDER.set(user);
    }

    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    public static Long getUserId() {
        UserInfo user = get();
        return user != null ? user.getUserId() : null;
    }

    public static String getProjectGroup() {
        UserInfo user = get();
        return user != null ? user.getProjectGroup() : null;
    }

    public static String getRole() {
        UserInfo user = get();
        return user != null ? user.getRole() : null;
    }

    @Data
    public static class UserInfo {
        private Long userId;
        private String account;
        private String projectGroup;
        private String role;

        public UserInfo(Long userId, String account, String projectGroup, String role) {
            this.userId = userId;
            this.account = account;
            this.projectGroup = projectGroup;
            this.role = role;
        }
    }
}
