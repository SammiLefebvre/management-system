package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.dto.LoginRequest;
import edu.cdut.aiback.dto.LoginResponse;
import edu.cdut.aiback.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String email) {
        authService.sendVerificationCode(email);
        return Result.ok("验证码已发送至邮箱");
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.ok(authService.loginByCode(request.getEmail(), request.getCode()));
    }

    /**
     * 微信小程序登录
     */
    @GetMapping("/wx-login")
    public Result<LoginResponse> wxLogin(@RequestParam String code) {
        return Result.ok(authService.wxLogin(code));
    }

    /**
     * 人脸识别登录
     */
    @PostMapping("/face-login")
    public Result<LoginResponse> faceLogin(@RequestBody Map<String, String> body) {
        return Result.ok(authService.faceLogin(body.get("imageBase64")));
    }
}
