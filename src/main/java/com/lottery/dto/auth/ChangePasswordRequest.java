package com.lottery.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordRequest {

    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码：8位以上且包含字母和数字 */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "新密码必须8位以上且包含字母和数字")
    private String newPassword;

    /** 确认新密码 */
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}
