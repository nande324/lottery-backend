package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库表 t_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（4-20位字母数字） */
    private String username;

    /** bcrypt 加密密码 */
    private String passwordHash;

    /** 角色：USER/ADMIN，默认 USER */
    @Builder.Default
    private String role = "USER";

    /** 账户状态：1正常 0锁定，默认 1 */
    @Builder.Default
    private Integer status = 1;

    /** 锁定截止时间 */
    private LocalDateTime lockUntil;

    /** 连续登录失败次数，默认 0 */
    @Builder.Default
    private Integer failCount = 0;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /** 逻辑删除标志：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
