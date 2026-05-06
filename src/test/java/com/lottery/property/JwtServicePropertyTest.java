package com.lottery.property;

import com.lottery.util.JwtUtil;
import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

/**
 * 属性9：JWT Token 信息完整性
 * Feature: lottery-management-system, Property 9: JWT Token 信息完整性
 * 验证需求：需求 9.6
 */
class JwtServicePropertyTest {

    private final JwtUtil jwtUtil;

    JwtServicePropertyTest() {
        jwtUtil = new JwtUtil();
        // 注入测试用的 secret 和 expiration（不依赖 Spring 容器）
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "lottery-management-system-secret-key-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24h
    }

    @Property(tries = 100)
    void tokenShouldContainCorrectUserInfo(
            @ForAll @StringLength(min = 4, max = 20) @AlphaChars String username,
            @ForAll("validUserIds") Long userId
    ) {
        String token = jwtUtil.generateToken(userId, username, "USER");

        // 解析 token
        Claims claims = jwtUtil.parseToken(token);

        // 验证 userId 和 username 与输入一致
        Assertions.assertEquals(username, claims.getSubject(),
                "Token 中的 username 应与输入一致");
        Assertions.assertEquals(userId, claims.get("userId", Long.class),
                "Token 中的 userId 应与输入一致");
    }

    @Property(tries = 100)
    void tokenShouldNotBeExpiredWithin24Hours(
            @ForAll @StringLength(min = 4, max = 20) @AlphaChars String username,
            @ForAll("validUserIds") Long userId
    ) {
        String token = jwtUtil.generateToken(userId, username, "USER");

        // Token 不应已过期
        Assertions.assertFalse(jwtUtil.isTokenExpired(token),
                "刚生成的 Token 不应已过期");

        // 过期时间应不早于当前时间 + 24h（允许1秒误差）
        Claims claims = jwtUtil.parseToken(token);
        Date expiration = claims.getExpiration();
        long expectedMinExpiry = System.currentTimeMillis() + 86400000L - 1000L;
        Assertions.assertTrue(expiration.getTime() >= expectedMinExpiry,
                "Token 过期时间应不早于当前时间 + 24h");
    }

    @Property(tries = 100)
    void tokenShouldContainCorrectRole(
            @ForAll @StringLength(min = 4, max = 20) @AlphaChars String username,
            @ForAll("validUserIds") Long userId,
            @ForAll("validRoles") String role
    ) {
        String token = jwtUtil.generateToken(userId, username, role);
        String extractedRole = jwtUtil.getRoleFromToken(token);
        Assertions.assertEquals(role, extractedRole,
                "Token 中的 role 应与输入一致");
    }

    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 100000L);
    }

    @Provide
    Arbitrary<String> validRoles() {
        return Arbitraries.of("USER", "ADMIN");
    }
}
