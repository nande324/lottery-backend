package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lottery.common.Result;
import com.lottery.dto.auth.ChangePasswordRequest;
import com.lottery.dto.auth.LoginRequest;
import com.lottery.dto.auth.RegisterRequest;
import com.lottery.entity.LoginLog;
import com.lottery.entity.User;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.LoginLogMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 用户认证服务
 * 处理注册、登录、修改密码等认证相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 成功响应
     */
    public Result<Void> register(RegisterRequest request) {
        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码不一致");
        }

        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new DuplicateKeyException("用户名已被占用");
        }

        // bcrypt 加密密码
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 构建用户实体并保存
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .role("USER")
                .status(1)
                .failCount(0)
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}", request.getUsername());

        return Result.success();
    }

    /**
     * 用户登录
     *
     * @param request   登录请求
     * @param ipAddress 客户端 IP
     * @return 包含 JWT Token 的成功响应，或错误响应
     */
    public Result<Map<String, Object>> login(LoginRequest request, String ipAddress) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        // 用户不存在，记录失败日志并返回 401
        if (user == null) {
            recordLoginLog(null, request.getUsername(), 0, ipAddress);
            return Result.error(401, "用户名或密码错误");
        }

        // 检查账户是否被锁定
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getLockUntil());
            return Result.error(401, "账户已锁定，请在 " + remainingSeconds + " 秒后重试");
        }

        // 若锁定时间已过，自动解锁
        if (user.getLockUntil() != null && user.getLockUntil().isBefore(LocalDateTime.now())) {
            user.setStatus(1);
            user.setFailCount(0);
            user.setLockUntil(null);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 密码不匹配，增加失败次数
            int newFailCount = user.getFailCount() + 1;
            user.setFailCount(newFailCount);

            // 连续失败 5 次，锁定账户 15 分钟
            if (newFailCount >= 5) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(15));
                user.setStatus(0);
                log.warn("用户 {} 连续登录失败 {} 次，账户已锁定15分钟", request.getUsername(), newFailCount);
            }

            // 更新用户状态
            userMapper.updateById(user);

            // 记录失败日志
            recordLoginLog(user.getId(), request.getUsername(), 0, ipAddress);

            return Result.error(401, "用户名或密码错误");
        }

        // 密码匹配，重置失败次数，解锁账户
        user.setFailCount(0);
        user.setStatus(1);
        user.setLockUntil(null);
        userMapper.updateById(user);

        // 记录成功登录日志
        recordLoginLog(user.getId(), user.getUsername(), 1, ipAddress);

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: {}", user.getUsername());

        return Result.success(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @param userId  当前用户 ID
     * @return 成功响应
     */
    public Result<Void> changePassword(ChangePasswordRequest request, Long userId) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户", userId);
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 校验两次新密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("两次新密码不一致");
        }

        // bcrypt 加密新密码并更新
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(encodedNewPassword);
        userMapper.updateById(user);

        log.info("用户 {} 修改密码成功", userId);

        return Result.success();
    }

    /**
     * 记录登录日志
     *
     * @param userId    用户 ID（失败时可为 null）
     * @param username  用户名
     * @param success   是否成功：1成功 0失败
     * @param ipAddress 客户端 IP
     */
    private void recordLoginLog(Long userId, String username, int success, String ipAddress) {
        LoginLog loginLog = LoginLog.builder()
                .userId(userId)
                .username(username)
                .success(success)
                .ipAddress(ipAddress)
                .loginTime(LocalDateTime.now())
                .build();
        loginLogMapper.insert(loginLog);
    }
}
