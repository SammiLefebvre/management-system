package edu.cdut.aiback.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求（支持邮箱验证码和账号密码两种方式）
 */
public class LoginRequest {

    @NotBlank(message = "邮箱/账号不能为空")
    private String email;

    private String code;
    private String account;
    private String password;

    public LoginRequest() {}

    public String getEmail() { return email != null ? email : account; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getAccount() { return account != null ? account : email; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
