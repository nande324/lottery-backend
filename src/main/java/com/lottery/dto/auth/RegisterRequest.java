package com.lottery.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户注册请求 DTO
 */
@Data
public class RegisterRequest {

    /** 用户名：4-20位字母数字 */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "用户名必须为4-20位字母数字")
    private String username;

    /** 密码：8位以上且包含字母和数字 */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "密码必须8位以上且包含字母和数字")
    private String password;

    /** 确认密码 */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
