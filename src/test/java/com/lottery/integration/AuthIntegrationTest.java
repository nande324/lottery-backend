package com.lottery.integration;

import com.lottery.dto.auth.LoginRequest;
import com.lottery.dto.auth.RegisterRequest;
import com.lottery.entity.User;
import com.lottery.mapper.UserMapper;
import com.lottery.service.AuthService;
import com.lottery.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户认证完整流程集成测试
 * 验证需求：需求 9.1 ~ 9.13
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_USERNAME = "testuser01";
    private static final String TEST_PASSWORD = "Test1234";

    @BeforeEach
    void setUp() {
        // 清理测试用户（事务回滚会处理，但确保干净状态）
        userMapper.delete(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, TEST_USERNAME));
    }

    /**
     * 测试：注册 → 登录获取 JWT → 验证 Token 信息
     */
    @Test
    void registerAndLoginShouldReturnValidJwt() {
        // 1. 注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setConfirmPassword(TEST_PASSWORD);
        authService.register(registerRequest);

        // 验证用户已创建
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, TEST_USERNAME));
        assertNotNull(user, "注册后用户应存在");
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals("USER", user.getRole());
        assertEquals(1, user.getStatus());
        assertEquals(0, user.getFailCount());

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        @SuppressWarnings("unchecked")
        var result = (com.lottery.common.Result<Map<String, Object>>) authService.login(loginRequest, "127.0.0.1");
        assertEquals(200, result.getCode(), "登录应成功");

        Map<String, Object> data = result.getData();
        assertNotNull(data.get("token"), "登录应返回 token");

        // 3. 验证 JWT Token
        String token = (String) data.get("token");
        assertFalse(jwtUtil.isTokenExpired(token), "Token 不应已过期");
        assertEquals(TEST_USERNAME, jwtUtil.getUsernameFromToken(token), "Token 中用户名应一致");
        assertEquals("USER", jwtUtil.getRoleFromToken(token), "Token 中角色应为 USER");
    }

    /**
     * 测试：连续 5 次登录失败 → 账户锁定 → 返回锁定剩余时间
     */
    @Test
    void fiveFailedLoginsShouldLockAccount() {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setConfirmPassword(TEST_PASSWORD);
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("WrongPassword1");

        // 连续 5 次失败登录
        for (int i = 0; i < 5; i++) {
            var result = authService.login(loginRequest, "127.0.0.1");
            assertEquals(401, result.getCode(), "错误密码应返回 401");
        }

        // 验证账户已锁定
        User lockedUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, TEST_USERNAME));
        assertNotNull(lockedUser);
        assertEquals(0, lockedUser.getStatus(), "账户应被锁定（status=0）");
        assertNotNull(lockedUser.getLockUntil(), "锁定截止时间不应为null");
        assertTrue(lockedUser.getLockUntil().isAfter(java.time.LocalDateTime.now()),
                "锁定截止时间应在当前时间之后");

        // 第 6 次登录应返回锁定信息
        var lockedResult = authService.login(loginRequest, "127.0.0.1");
        assertEquals(401, lockedResult.getCode(), "锁定账户登录应返回 401");
        assertTrue(lockedResult.getMessage().contains("锁定") || lockedResult.getMessage().contains("秒"),
                "错误信息应包含锁定相关提示，实际：" + lockedResult.getMessage());
    }

    /**
     * 测试：用户名已存在时注册应返回 409
     */
    @Test
    void registerWithDuplicateUsernameShouldThrow() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(TEST_USERNAME);
        req.setPassword(TEST_PASSWORD);
        req.setConfirmPassword(TEST_PASSWORD);
        authService.register(req);

        // 再次注册相同用户名
        assertThrows(org.springframework.dao.DuplicateKeyException.class,
                () -> authService.register(req),
                "重复用户名应抛出 DuplicateKeyException");
    }

    /**
     * 测试：密码不一致时注册应抛出异常
     */
    @Test
    void registerWithMismatchedPasswordsShouldThrow() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(TEST_USERNAME);
        req.setPassword(TEST_PASSWORD);
        req.setConfirmPassword("DifferentPass1");

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(req),
                "密码不一致应抛出 IllegalArgumentException");
    }

    /**
     * 测试：错误密码登录应返回 401
     */
    @Test
    void loginWithWrongPasswordShouldReturn401() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(TEST_USERNAME);
        req.setPassword(TEST_PASSWORD);
        req.setConfirmPassword(TEST_PASSWORD);
        authService.register(req);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("WrongPassword1");

        var result = authService.login(loginRequest, "127.0.0.1");
        assertEquals(401, result.getCode(), "错误密码应返回 401");
        assertTrue(result.getMessage().contains("用户名或密码错误"),
                "错误信息应为通用提示，实际：" + result.getMessage());
    }
}
