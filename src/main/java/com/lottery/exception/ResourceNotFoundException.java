package com.lottery.exception;

/**
 * 资源不存在异常
 * 当请求的资源在数据库中不存在时抛出此异常
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " 不存在，ID: " + id);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
