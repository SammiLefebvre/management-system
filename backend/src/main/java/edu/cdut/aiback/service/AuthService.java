package edu.cdut.aiback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.cdut.aiback.cache.VerificationCodeCache;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.dto.FaceRecognizeResponse;
import edu.cdut.aiback.dto.LoginResponse;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.util.JwtUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JavaMailSender mailSender;
    private final VerificationCodeCache codeCache;
    private final JwtUtil jwtUtil;
    private final PersonnelService personnelService;
    private final FaceService faceService;

    @Value("${captcha.expire}")
    private long expireSeconds;

    @Value("${captcha.length}")
    private int codeLength;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public AuthService(JavaMailSender mailSender, VerificationCodeCache codeCache,
                       JwtUtil jwtUtil, PersonnelService personnelService,
                       FaceService faceService) {
        this.mailSender = mailSender;
        this.codeCache = codeCache;
        this.jwtUtil = jwtUtil;
        this.personnelService = personnelService;
        this.faceService = faceService;
    }

    public void sendVerificationCode(String email) {
        String code = RandomStringUtils.randomNumeric(codeLength);
        codeCache.put(email, code, expireSeconds);

        // TODO: 测试期间输出验证码，生产环境请删除或改用日志级别控制
        System.out.println("[TEST-CODE] email=" + email + ", code=" + code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【工单管理系统】登录验证码");
        message.setText("您的验证码是：" + code + "，有效期 " + expireSeconds + " 秒。请勿泄露给他人。");
        mailSender.send(message);
    }

    /**
     * 邮箱验证码登录 —— 查找 personnel 表，生成含角色信息的 token
     */
    public LoginResponse loginByCode(String email, String code) {
        String cachedCode = codeCache.get(email);

        // TODO: 测试专用，生产环境请删除下面这行
        if (!"123456".equals(code)) {
            if (cachedCode == null) {
                throw new BizException(401, "验证码已过期，请重新获取");
            }
            if (!cachedCode.equals(code)) {
                throw new BizException(401, "验证码错误");
            }
        }
        codeCache.remove(email);

        // 按账号(邮箱)查找用户
        Personnel personnel = personnelService.getOne(
                new LambdaQueryWrapper<Personnel>().eq(Personnel::getAccount, email));
        if (personnel == null) {
            throw new BizException(401, "用户不存在: " + email);
        }

        String token = jwtUtil.generateToken(
                personnel.getId(), personnel.getAccount(),
                personnel.getProjectGroup(), personnel.getRole());

        LoginResponse resp = new LoginResponse(true, token, "登录成功");
        resp.setUserId(personnel.getId());
        resp.setAccount(personnel.getAccount());
        resp.setRole(personnel.getRole());
        resp.setProjectGroup(personnel.getProjectGroup());
        return resp;
    }

    /**
     * 微信小程序一键登录 —— 通过 wx.login code 换取 OpenID，关联 personnel 表
     */
    public LoginResponse wxLogin(String wxCode) {
        // TODO: 调用微信接口 code2Session 获取 openid
        // 暂时用 wxCode 作为 openid 测试
        String openId = wxCode;

        Personnel personnel = personnelService.getByWxOpenId(openId);
        if (personnel == null) {
            throw new BizException(401, "未绑定微信，请联系管理员");
        }

        String token = jwtUtil.generateToken(
                personnel.getId(), personnel.getAccount(),
                personnel.getProjectGroup(), personnel.getRole());

        LoginResponse resp = new LoginResponse(true, token, "登录成功");
        resp.setUserId(personnel.getId());
        resp.setAccount(personnel.getAccount());
        resp.setRole(personnel.getRole());
        resp.setProjectGroup(personnel.getProjectGroup());
        return resp;
    }

    /**
     * 人脸识别登录
     */
    public LoginResponse faceLogin(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BizException("人脸图片不能为空");
        }
        FaceRecognizeResponse recognize = faceService.recognizeFaceBase64(imageBase64, "gzgd_users");
        if (!recognize.samePerson()) {
            throw new BizException("人脸识别失败，请使用验证码登录");
        }
        String account = recognize.userId();
        Personnel personnel = personnelService.getOne(
                new LambdaQueryWrapper<Personnel>().eq(Personnel::getAccount, account));
        if (personnel == null) {
            throw new BizException("未找到匹配账号: " + account);
        }

        String token = jwtUtil.generateToken(
                personnel.getId(), personnel.getAccount(),
                personnel.getProjectGroup(), personnel.getRole());

        LoginResponse resp = new LoginResponse(true, token, "登录成功");
        resp.setUserId(personnel.getId());
        resp.setAccount(personnel.getAccount());
        resp.setRole(personnel.getRole());
        resp.setProjectGroup(personnel.getProjectGroup());
        return resp;
    }
}
