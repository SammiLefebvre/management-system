package edu.cdut.aiback.interceptor;

import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器 —— 拦截 /api/**，放行 /api/auth/**
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或 Token 缺失\"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token 无效或已过期\"}");
            return false;
        }

        // 解析用户信息并写入 ThreadLocal
        Claims claims = jwtUtil.getClaimsFromToken(token);
        Long userId = claims.get("userId", Long.class);
        String account = claims.getSubject();
        String projectGroup = claims.get("projectGroup", String.class);
        String role = claims.get("role", String.class);

        UserContext.set(new UserContext.UserInfo(userId, account, projectGroup, role));

        // 也保留到 request attribute（兼容旧代码）
        request.setAttribute("userId", userId);
        request.setAttribute("account", account);
        request.setAttribute("loginEmail", account);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
