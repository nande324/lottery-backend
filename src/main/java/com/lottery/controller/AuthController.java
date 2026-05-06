package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.auth.ChangePasswordRequest;
import com.lottery.dto.auth.LoginRequest;
import com.lottery.dto.auth.RegisterRequest;
import com.lottery.service.AuthService;
import com.lottery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 用户认证控制器
 * 提供注册、登录、登出、修改密码接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求体
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 用户登录
     *
     * @param request     登录请求体
     * @param httpRequest HTTP 请求（用于获取客户端 IP）
     * @return 包含 JWT Token 的登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return authService.login(request, ipAddress);
    }

    /**
     * 用户登出
     * 后端无状态，前端清除 Token 即可
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 修改密码（需要认证）
     *
     * @param request 修改密码请求体
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return authService.changePassword(request, userId);
    }

    /**
     * 获取客户端真实 IP 地址
     * 优先从代理头中获取，其次使用 RemoteAddr
     *
     * @param request HTTP 请求
     * @return 客户端 IP 字符串
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
