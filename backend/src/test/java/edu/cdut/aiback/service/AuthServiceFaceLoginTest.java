package edu.cdut.aiback.service;

import edu.cdut.aiback.cache.VerificationCodeCache;
import edu.cdut.aiback.dto.FaceRecognizeResponse;
import edu.cdut.aiback.dto.LoginResponse;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceFaceLoginTest {

    @Test
    void faceLogin_shouldReturnTokenWhenFaceMatches() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        VerificationCodeCache cache = mock(VerificationCodeCache.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PersonnelService personnelService = mock(PersonnelService.class);
        FaceService faceService = mock(FaceService.class);

        AuthService authService = new AuthService(mailSender, cache, jwtUtil, personnelService, faceService);

        when(faceService.recognizeFaceBase64(any(), eq("gzgd_users")))
                .thenReturn(new FaceRecognizeResponse(true, 95.0, "field@gzgd.com", "gzgd_users", "ok"));

        when(jwtUtil.generateToken(any(), any(), any(), any())).thenReturn("mock-token");

        Personnel p = new Personnel();
        p.setId(3L);
        p.setAccount("field@gzgd.com");
        p.setName("外场工程师");
        p.setRole("外场");
        p.setProjectGroup("演示项目组");
        when(personnelService.getOne(any())).thenReturn(p);

        LoginResponse resp = authService.faceLogin("base64-image");

        assertTrue(resp.isSuccess());
        assertEquals("field@gzgd.com", resp.getAccount());
        assertNotNull(resp.getToken());
    }
}
