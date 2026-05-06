package com.lottery.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 工具类
 * 提供从 SecurityContextHolder 获取当前用户信息的静态方法
 */
public class SecurityUtil {

    private SecurityUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 获取当前已认证用户的 ID
     * JWT 过滤器将 userId 存入 authentication.details
     *
     * @return 当前用户 ID
     * @throws IllegalStateException 若未认证或 details 不合法
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("当前请求未认证");
        }
        Object details = authentication.getDetails();
        if (details instanceof Long) {
            return (Long) details;
        }
        if (details instanceof Integer) {
            return ((Integer) details).longValue();
        }
        throw new IllegalStateException("无法获取当前用户 ID");
    }
}
