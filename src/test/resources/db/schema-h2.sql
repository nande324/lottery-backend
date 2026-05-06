-- H2 兼容版本的建表脚本（用于集成测试）

DROP TABLE IF EXISTS t_login_log;
DROP TABLE IF EXISTS t_scratch_ticket;
DROP TABLE IF EXISTS t_draw_result;
DROP TABLE IF EXISTS t_draw_ticket;
DROP TABLE IF EXISTS t_lottery_mode;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(20)  NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER',
    status        TINYINT      NOT NULL DEFAULT 1,
    lock_until    DATETIME     NULL,
    fail_count    INT          NOT NULL DEFAULT 0,
    created_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE t_lottery_mode (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(20)   NOT NULL,
    code          VARCHAR(20)   NOT NULL UNIQUE,
    type          VARCHAR(10)   NOT NULL,
    red_count     INT           NULL,
    red_min       INT           NULL,
    red_max       INT           NULL,
    blue_count    INT           NULL DEFAULT 0,
    blue_min      INT           NULL,
    blue_max      INT           NULL,
    ticket_price  DECIMAL(6,2)  NOT NULL DEFAULT 2.00,
    description   VARCHAR(200)  NULL,
    sort_order    INT           NOT NULL DEFAULT 0,
    is_preset     TINYINT       NOT NULL DEFAULT 0,
    created_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE t_draw_ticket (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    mode_id       BIGINT         NOT NULL,
    issue_no      VARCHAR(20)    NULL,
    red_numbers   VARCHAR(50)    NOT NULL,
    blue_numbers  VARCHAR(20)    NULL,
    bet_amount    DECIMAL(8,2)   NOT NULL,
    bet_time      DATETIME       NOT NULL,
    win_status    VARCHAR(10)    NOT NULL DEFAULT 'PENDING',
    win_level     INT            NULL,
    win_amount    DECIMAL(10,2)  NULL DEFAULT 0,
    draw_time     DATETIME       NULL,
    is_claimed    TINYINT        NOT NULL DEFAULT 0,
    is_fixed      TINYINT        NOT NULL DEFAULT 0,
    remark        VARCHAR(200)   NULL,
    created_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE t_draw_result (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    mode_id       BIGINT         NOT NULL,
    issue_no      VARCHAR(20)    NOT NULL,
    draw_date     DATE           NOT NULL,
    red_numbers   VARCHAR(50)    NOT NULL,
    blue_numbers  VARCHAR(20)    NULL,
    prize_pool    DECIMAL(14,2)  NULL,
    source        VARCHAR(10)    NOT NULL DEFAULT 'MANUAL',
    created_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_mode_issue (user_id, mode_id, issue_no)
);

CREATE TABLE t_scratch_ticket (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    user_id       BIGINT        NOT NULL,
    scratch_date  DATE          NOT NULL,
    scratch_type  VARCHAR(50)   NOT NULL,
    cost_amount   DECIMAL(8,2)  NOT NULL,
    win_amount    DECIMAL(8,2)  NOT NULL DEFAULT 0,
    remark        VARCHAR(200)  NULL,
    created_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE t_login_log (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NULL,
    username     VARCHAR(20) NOT NULL,
    success      TINYINT     NOT NULL,
    ip_address   VARCHAR(45) NULL,
    login_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
