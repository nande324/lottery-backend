-- ============================================================
-- 彩票管理系统 预置数据脚本
-- 使用 INSERT IGNORE INTO 避免重复插入
-- ============================================================

-- 预置彩票模式数据
INSERT IGNORE INTO `t_lottery_mode`
    (`name`, `code`, `type`, `red_count`, `red_min`, `red_max`, `blue_count`, `blue_min`, `blue_max`, `ticket_price`, `sort_order`, `is_preset`, `description`)
VALUES
    ('双色球', 'SSQ', 'DRAW',    6, 1,    33, 1, 1,    16, 2.00, 1, 1, '从1-33中选6个红球，从1-16中选1个蓝球'),
    ('大乐透', 'DLT', 'DRAW',    5, 1,    35, 2, 1,    12, 2.00, 2, 1, '从1-35中选5个前区号码，从1-12中选2个后区号码'),
    ('七乐彩', 'QLC', 'DRAW',    7, 1,    30, 0, NULL, NULL, 2.00, 3, 1, '从1-30中选7个号码'),
    ('排列三', 'PL3', 'DRAW',    3, 0,    9,  0, NULL, NULL, 2.00, 4, 1, '从0-9中各选1个数字，共3位，允许重复'),
    ('排列五', 'PL5', 'DRAW',    5, 0,    9,  0, NULL, NULL, 2.00, 5, 1, '从0-9中各选1个数字，共5位，允许重复'),
    ('刮刮乐', 'SCR', 'SCRATCH', 0, NULL, NULL, 0, NULL, NULL, 0.00, 6, 1, '刮刮乐彩票，手动录入消费和中奖金额');

-- 预置中奖规则数据
-- 注意：mode_id 依赖 t_lottery_mode 的自增 ID，此处使用子查询按 code 关联

-- 双色球（SSQ）中奖规则
INSERT IGNORE INTO `t_win_rule`
    (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `hit_condition`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
SELECT id, 1, '一等奖', 6, 1, NULL,          'POOL',  NULL,    '6红+1蓝，浮动奖金', 1 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
UNION ALL
SELECT id, 2, '二等奖', 6, 0, NULL,          'POOL',  NULL,    '6红，浮动奖金', 2 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
UNION ALL
SELECT id, 3, '三等奖', 5, 1, NULL,          'FIXED', 3000.00, '5红+1蓝，固定3000元', 3 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
UNION ALL
SELECT id, 4, '四等奖', 4, 1, '5红 或 4红+1蓝', 'FIXED', 200.00,  '5红 或 4红+1蓝，固定200元', 4 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
UNION ALL
SELECT id, 5, '五等奖', 3, 1, '4红 或 3红+1蓝', 'FIXED', 10.00,   '4红 或 3红+1蓝，固定10元', 5 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
UNION ALL
SELECT id, 6, '六等奖', 0, 1, '任意红球+1蓝',   'FIXED', 5.00,    '蓝球命中1，固定5元', 6 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0;

-- 大乐透（DLT）中奖规则
INSERT IGNORE INTO `t_win_rule`
    (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `hit_condition`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
SELECT id, 1,  '一等奖',   5, 2, NULL, 'POOL',  NULL,     '5前区+2后区，浮动奖金', 1  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 2,  '二等奖',   5, 1, NULL, 'POOL',  NULL,     '5前区+1后区，浮动奖金', 2  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 3,  '三等奖',   5, 0, NULL, 'FIXED', 10000.00, '5前区，固定10000元', 3  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 4,  '四等奖',   4, 2, NULL, 'FIXED', 3000.00,  '4前区+2后区，固定3000元', 4  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 5,  '五等奖',   4, 1, NULL, 'FIXED', 300.00,   '4前区+1后区，固定300元', 5  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 6,  '六等奖',   3, 2, NULL, 'FIXED', 200.00,   '3前区+2后区，固定200元', 6  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 7,  '七等奖',   4, 0, NULL, 'FIXED', 100.00,   '4前区，固定100元', 7  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 8,  '八等奖',   3, 1, NULL, 'FIXED', 15.00,    '3前区+1后区，固定15元', 8  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 9,  '九等奖',   2, 2, NULL, 'FIXED', 15.00,    '2前区+2后区，固定15元', 9  FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 10, '十等奖',   3, 0, NULL, 'FIXED', 5.00,     '3前区，固定5元', 10 FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 11, '十一等奖', 1, 2, NULL, 'FIXED', 5.00,     '1前区+2后区，固定5元', 11 FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0
UNION ALL
SELECT id, 12, '十二等奖', 0, 2, NULL, 'FIXED', 5.00,     '2后区，固定5元', 12 FROM `t_lottery_mode` WHERE `code` = 'DLT' AND `deleted` = 0;

-- 七乐彩（QLC）中奖规则
INSERT IGNORE INTO `t_win_rule`
    (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `hit_condition`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
SELECT id, 1, '一等奖', 7, 0, NULL, 'POOL',  NULL,    '7个号码全中，浮动奖金', 1 FROM `t_lottery_mode` WHERE `code` = 'QLC' AND `deleted` = 0
UNION ALL
SELECT id, 2, '二等奖', 6, 0, NULL, 'POOL',  NULL,    '6个号码命中，浮动奖金', 2 FROM `t_lottery_mode` WHERE `code` = 'QLC' AND `deleted` = 0
UNION ALL
SELECT id, 3, '三等奖', 5, 0, NULL, 'FIXED', 1000.00, '5个号码命中，固定1000元', 3 FROM `t_lottery_mode` WHERE `code` = 'QLC' AND `deleted` = 0
UNION ALL
SELECT id, 4, '四等奖', 4, 0, NULL, 'FIXED', 100.00,  '4个号码命中，固定100元', 4 FROM `t_lottery_mode` WHERE `code` = 'QLC' AND `deleted` = 0
UNION ALL
SELECT id, 5, '五等奖', 3, 0, NULL, 'FIXED', 10.00,   '3个号码命中，固定10元', 5 FROM `t_lottery_mode` WHERE `code` = 'QLC' AND `deleted` = 0;

-- 排列三（PL3）中奖规则
INSERT IGNORE INTO `t_win_rule`
    (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `hit_condition`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
SELECT id, 1, '直选', 3, 0, '3位数字顺序完全一致', 'FIXED', 1000.00, '直选：3位数字顺序完全一致，固定1000元', 1 FROM `t_lottery_mode` WHERE `code` = 'PL3' AND `deleted` = 0
UNION ALL
SELECT id, 2, '组选三', 2, 0, '含重复数字的3位数，任意顺序', 'FIXED', 320.00, '组选三：含重复数字，任意顺序，固定320元', 2 FROM `t_lottery_mode` WHERE `code` = 'PL3' AND `deleted` = 0
UNION ALL
SELECT id, 3, '组选六', 1, 0, '3个不同数字，任意顺序', 'FIXED', 160.00, '组选六：3个不同数字，任意顺序，固定160元', 3 FROM `t_lottery_mode` WHERE `code` = 'PL3' AND `deleted` = 0;

-- 排列五（PL5）中奖规则
INSERT IGNORE INTO `t_win_rule`
    (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `hit_condition`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
SELECT id, 1, '直选', 5, 0, '5位数字顺序完全一致', 'FIXED', 100000.00, '直选：5位数字顺序完全一致，固定100000元', 1 FROM `t_lottery_mode` WHERE `code` = 'PL5' AND `deleted` = 0;
