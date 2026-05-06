package com.lottery.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义认证入口点
 * 当未登录或 Token 已过期时，返回 JSON 格式的 401 响应
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> result = Result.error(401, "未登录或 Token 已过期");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
