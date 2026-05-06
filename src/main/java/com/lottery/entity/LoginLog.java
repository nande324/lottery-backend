package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 登录日志实体类，对应数据库表 t_login_log（无逻辑删除）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_login_log")
public class LoginLog {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（登录失败时可能为 null） */
    private Long userId;

    /** 尝试登录的用户名 */
    private String username;

    /** 是否成功：1成功 0失败 */
    private Integer success;

    /** 客户端IP */
    private String ipAddress;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
