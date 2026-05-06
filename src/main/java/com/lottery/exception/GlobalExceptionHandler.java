package com.lottery.exception;

import com.lottery.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回标准响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败异常
     * 返回 HTTP 400，包含具体字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put("global", error.getDefaultMessage());
            }
        });
        log.debug("参数校验失败: {}", errors);
        Result<Map<String, String>> result = Result.error(400, "参数校验失败");
        result.setData(errors);
        return result;
    }

    /**
     * 处理数据库唯一键冲突异常
     * 返回 HTTP 409，提示数据已存在
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.debug("数据重复: {}", ex.getMessage());
        return Result.error(409, "数据已存在");
    }

    /**
     * 处理资源不存在异常
     * 返回 HTTP 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.debug("资源不存在: {}", ex.getMessage());
        return Result.error(404, ex.getMessage());
    }

    /**
     * 处理权限不足异常
     * 返回 HTTP 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("权限不足: {}", ex.getMessage());
        return Result.error(403, "无权限访问该资源");
    }

    /**
     * 处理非法参数异常（如两次密码不一致、原密码错误等）
     * 返回 HTTP 400，message 为异常消息
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.debug("非法参数: {}", ex.getMessage());
        return Result.error(400, ex.getMessage());
    }

    /**
     * 处理其他未预期异常
     * 返回 HTTP 500，记录完整日志，不向客户端暴露堆栈信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("服务器内部错误", ex);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
