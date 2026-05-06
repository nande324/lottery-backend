package com.lottery.common;

import lombok.Data;

/**
 * 统一响应体
 * 所有接口统一返回格式：{ "code": int, "message": string, "data": object }
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** 响应状态码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    private Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应体
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @return 成功响应体
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 错误响应
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应体
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
