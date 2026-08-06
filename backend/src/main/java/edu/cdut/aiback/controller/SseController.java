package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final JwtUtil jwtUtil;
    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public SseEmitter dashboard(@RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token 无效");
        }
        Claims claims = jwtUtil.getClaimsFromToken(token);
        UserContext.UserInfo userInfo = new UserContext.UserInfo(
                claims.get("userId", Long.class),
                claims.getSubject(),
                claims.get("projectGroup", String.class),
                claims.get("role", String.class)
        );

        SseEmitter emitter = new SseEmitter(0L);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                UserContext.set(userInfo);
                emitter.send(SseEmitter.event().name("stats").data(statisticsService.getDashboardStatistics()));
            } catch (IOException e) {
                emitter.completeWithError(e);
                executor.shutdown();
            } finally {
                UserContext.clear();
            }
        }, 0, 30, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        emitter.onError((e) -> executor.shutdown());
        return emitter;
    }
}
