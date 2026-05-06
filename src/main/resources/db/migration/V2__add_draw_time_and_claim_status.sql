-- 为摇奖票表添加开奖时间和兑奖状态字段
-- V2__add_draw_time_and_claim_status.sql

-- 添加开奖时间字段
ALTER TABLE `t_draw_ticket` 
ADD COLUMN `draw_time` DATETIME NULL COMMENT '开奖时间（匹配开奖结果时自动保存）' 
AFTER `win_amount`;

-- 添加兑奖状态字段
ALTER TABLE `t_draw_ticket` 
ADD COLUMN `is_claimed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已兑奖：0未兑奖 1已兑奖' 
AFTER `draw_time`;