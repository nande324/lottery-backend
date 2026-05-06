package com.lottery.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义访问拒绝处理器
 * 当已登录用户访问无权限资源时，返回 JSON 格式的 403 响应
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> result = Result.error(403, "无权限访问该资源");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
