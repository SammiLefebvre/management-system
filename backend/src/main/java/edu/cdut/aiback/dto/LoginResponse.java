package edu.cdut.aiback.dto;

/**
 * 登录响应
 */
public class LoginResponse {

    private boolean success;
    private String token;
    private String message;
    private Long userId;
    private String account;
    private String role;
    private String projectGroup;

    public LoginResponse() {}

    public LoginResponse(boolean success, String token, String message) {
        this.success = success;
        this.token = token;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getProjectGroup() { return projectGroup; }
    public void setProjectGroup(String projectGroup) { this.projectGroup = projectGroup; }
}
