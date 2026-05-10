-- ============================================================
-- 彩票管理系统 数据库建表脚本
-- 数据库：MySQL 8.x
-- 字符集：utf8mb4
-- ============================================================

-- 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(20)  NOT NULL UNIQUE COMMENT '用户名（4-20位字母数字）',
    `password_hash` VARCHAR(60)  NOT NULL COMMENT 'bcrypt 加密密码',
    `role`          VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `status`        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '账户状态：1正常 0锁定',
    `lock_until`    DATETIME     NULL COMMENT '锁定截止时间',
    `fail_count`    INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `created_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 彩票模式表
DROP TABLE IF EXISTS `t_lottery_mode`;
CREATE TABLE `t_lottery_mode` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(20)   NOT NULL COMMENT '模式名称（如双色球）',
    `code`          VARCHAR(20)   NOT NULL UNIQUE COMMENT '模式编码（如SSQ）',
    `type`          VARCHAR(10)   NOT NULL COMMENT '类型：DRAW（摇奖）/ SCRATCH（刮刮乐）',
    `red_count`     INT           NULL COMMENT '红球数量',
    `red_min`       INT           NULL COMMENT '红球最小值',
    `red_max`       INT           NULL COMMENT '红球最大值',
    `blue_count`    INT           NULL DEFAULT 0 COMMENT '蓝球数量（0表示无蓝球）',
    `blue_min`      INT           NULL COMMENT '蓝球最小值',
    `blue_max`      INT           NULL COMMENT '蓝球最大值',
    `ticket_price`  DECIMAL(6,2)  NOT NULL DEFAULT 2.00 COMMENT '单注票价（元）',
    `description`   VARCHAR(200)  NULL COMMENT '规则说明',
    `sort_order`    INT           NOT NULL DEFAULT 0 COMMENT '排序权重',
    `is_preset`     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否预置模式：0否 1是',
    `created_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='彩票模式表';

-- 中奖规则表
DROP TABLE IF EXISTS `t_win_rule`;
CREATE TABLE `t_win_rule` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `mode_id`       BIGINT         NOT NULL COMMENT '彩票模式ID（关联 t_lottery_mode）',
    `win_level`     INT            NOT NULL COMMENT '中奖等级（1=一等奖，数字越小奖级越高）',
    `level_name`    VARCHAR(20)    NOT NULL COMMENT '等级名称（如一等奖、二等奖）',
    `red_hit`       INT            NOT NULL COMMENT '红球命中数',
    `blue_hit`      INT            NOT NULL DEFAULT 0 COMMENT '蓝球命中数（无蓝球模式填0）',
    `hit_condition` VARCHAR(100)   NULL COMMENT '命中条件补充说明（如"5红+1蓝 或 4红+1蓝"）',
    `prize_type`    VARCHAR(10)    NOT NULL COMMENT '奖金类型：FIXED（固定金额）/ POOL（浮动/奖池）',
    `fixed_amount`  DECIMAL(10,2)  NULL COMMENT '固定奖金金额（prize_type=FIXED时有效，元）',
    `description`   VARCHAR(200)   NULL COMMENT '规则说明',
    `sort_order`    INT            NOT NULL DEFAULT 0 COMMENT '同模式内排序权重',
    `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_mode_level` (`mode_id`, `win_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中奖规则表';

-- 摇奖票表
DROP TABLE IF EXISTS `t_draw_ticket`;
CREATE TABLE `t_draw_ticket` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT         NOT NULL COMMENT '所属用户ID',
    `mode_id`       BIGINT         NOT NULL COMMENT '彩票模式ID',
    `issue_no`      VARCHAR(20)    NULL COMMENT '期号',
    `red_numbers`   VARCHAR(50)    NOT NULL COMMENT '红球号码（逗号分隔，有序）',
    `blue_numbers`  VARCHAR(20)    NULL COMMENT '蓝球号码（逗号分隔）',
    `bet_amount`    DECIMAL(8,2)   NOT NULL COMMENT '投注金额（元）',
    `bet_time`      DATETIME       NOT NULL COMMENT '投注时间',
    `win_status`    VARCHAR(10)    NOT NULL DEFAULT 'PENDING' COMMENT '中奖状态：PENDING/NO_WIN/WIN',
    `win_level`     INT            NULL COMMENT '中奖等级（1=一等奖，依此类推）',
    `win_amount`    DECIMAL(10,2)  NULL DEFAULT 0 COMMENT '中奖金额（元）',
    `draw_time`     DATETIME       NULL COMMENT '开奖时间（匹配开奖结果时自动保存）',
    `is_claimed`    TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否已兑奖：0未兑奖 1已兑奖',
    `is_fixed`      TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否固定号码：0否 1是',
    `remark`        VARCHAR(200)   NULL COMMENT '备注',
    `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_mode_issue` (`mode_id`, `issue_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摇奖票表';

-- 开奖结果表
DROP TABLE IF EXISTS `t_draw_result`;
CREATE TABLE `t_draw_result` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT         NOT NULL COMMENT '录入用户ID（数据隔离）',
    `mode_id`       BIGINT         NOT NULL COMMENT '彩票模式ID',
    `issue_no`      VARCHAR(20)    NOT NULL COMMENT '期号',
    `draw_date`     DATE           NOT NULL COMMENT '开奖日期',
    `red_numbers`   VARCHAR(50)    NOT NULL COMMENT '开奖红球号码（逗号分隔，有序）',
    `blue_numbers`  VARCHAR(20)    NULL COMMENT '开奖蓝球号码（逗号分隔）',
    `prize_pool`    DECIMAL(14,2)  NULL COMMENT '奖池金额（元，可选）',
    `source`        VARCHAR(10)    NOT NULL DEFAULT 'MANUAL' COMMENT '数据来源：MANUAL/API',
    `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_mode_issue` (`user_id`, `mode_id`, `issue_no`),
    INDEX `idx_user_mode` (`user_id`, `mode_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开奖结果表';

-- 刮刮乐记录表
DROP TABLE IF EXISTS `t_scratch_ticket`;
CREATE TABLE `t_scratch_ticket` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT        NOT NULL COMMENT '所属用户ID',
    `scratch_date`  DATE          NOT NULL COMMENT '刮奖日期',
    `scratch_type`  VARCHAR(50)   NOT NULL COMMENT '刮刮乐类型/名称',
    `cost_amount`   DECIMAL(8,2)  NOT NULL COMMENT '消费金额（元）',
    `win_amount`    DECIMAL(8,2)  NOT NULL DEFAULT 0 COMMENT '中奖金额（元）',
    `remark`        VARCHAR(200)  NULL COMMENT '备注',
    `created_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_date` (`user_id`, `scratch_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刮刮乐记录表';

-- 官方历史开奖结果表（公共表，不按用户隔离，从外部接口同步）
DROP TABLE IF EXISTS `t_official_draw_result`;
CREATE TABLE `t_official_draw_result` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `lottery_id`    INT            NOT NULL DEFAULT 1 COMMENT '彩票ID（1=双色球）',
    `issue_no`      VARCHAR(20)    NOT NULL COMMENT '期号',
    `draw_date`     DATE           NOT NULL COMMENT '开奖日期',
    `red_numbers`   VARCHAR(50)    NOT NULL COMMENT '开奖红球号码（逗号分隔，升序）',
    `blue_numbers`  VARCHAR(20)    NULL COMMENT '开奖蓝球号码（逗号分隔）',
    `prize_pool`    DECIMAL(14,2)  NULL COMMENT '奖池金额（元，可选）',
    `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lottery_issue` (`lottery_id`, `issue_no`),
    INDEX `idx_issue_no` (`issue_no`),
    INDEX `idx_draw_date` (`draw_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='官方历史开奖结果表（公共，从外部接口同步）';

-- 默认号码表
DROP TABLE IF EXISTS `t_default_number`;
CREATE TABLE `t_default_number` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `mode_id`       BIGINT         NOT NULL COMMENT '彩票模式ID（关联 t_lottery_mode）',
    `name`          VARCHAR(50)    NOT NULL COMMENT '默认号码名称（如：生日号、幸运数字）',
    `red_numbers`   VARCHAR(50)    NOT NULL COMMENT '红球号码（逗号分隔，有序）',
    `blue_numbers`  VARCHAR(20)    NULL COMMENT '蓝球号码（逗号分隔）',
    `sort_order`    INT            NOT NULL DEFAULT 0 COMMENT '排序权重',
    `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_mode_id` (`mode_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默认号码表（用户预设投注号码）';

-- 登录日志表
DROP TABLE IF EXISTS `t_login_log`;
CREATE TABLE `t_login_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT       NULL COMMENT '用户ID（登录失败时可能为NULL）',
    `username`     VARCHAR(20)  NOT NULL COMMENT '尝试登录的用户名',
    `success`      TINYINT(1)   NOT NULL COMMENT '是否成功：1成功 0失败',
    `ip_address`   VARCHAR(45)  NULL COMMENT '客户端IP',
    `login_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    INDEX `idx_username_time` (`username`, `login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
