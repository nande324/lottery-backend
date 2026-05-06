package com.lottery.config;

import com.lottery.entity.OfficialDrawResult;
import com.lottery.mapper.OfficialDrawResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 测试数据初始化器
 * 在应用启动时插入一些测试用的官方开奖结果数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // 在DatabaseMigration之后执行
public class TestDataInitializer implements CommandLineRunner {

    private final OfficialDrawResultMapper officialDrawResultMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 首先检查并创建缺失的表
        ensureWinRuleTableExists();
        
        // 检查是否已有测试数据
        long count = officialDrawResultMapper.selectCount(null);
        if (count > 0) {
            log.info("官方开奖结果表已有数据，跳过测试数据初始化");
            return;
        }

        log.info("开始初始化测试用的官方开奖结果数据...");

        // 插入测试数据
        insertTestData("2024001", "2024-01-02", "01,07,12,18,25,33", "16");
        insertTestData("2024002", "2024-01-04", "03,09,15,21,28,32", "08");
        insertTestData("2024003", "2024-01-07", "05,11,17,23,29,31", "12");
        insertTestData("2024004", "2024-01-09", "02,08,14,20,26,30", "04");
        insertTestData("2024005", "2024-01-11", "06,10,16,22,27,33", "15");

        log.info("测试数据初始化完成");
    }

    /**
     * 确保中奖规则表存在
     */
    private void ensureWinRuleTableExists() {
        try {
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 't_win_rule'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableCount == null || tableCount == 0) {
                log.info("中奖规则表不存在，开始创建...");
                createWinRuleTable();
                insertWinRuleData();
                log.info("中奖规则表创建完成");
            } else {
                log.debug("中奖规则表已存在");
            }
        } catch (Exception e) {
            log.error("检查/创建中奖规则表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建中奖规则表
     */
    private void createWinRuleTable() {
        String createTableSql = """
            CREATE TABLE `t_win_rule` (
                `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
                `mode_id`       BIGINT         NOT NULL COMMENT '彩票模式ID（关联 t_lottery_mode）',
                `win_level`     INT            NOT NULL COMMENT '中奖等级（1=一等奖，数字越小等级越高）',
                `level_name`    VARCHAR(20)    NOT NULL COMMENT '等级名称（如"三等奖"）',
                `red_hit`       INT            NOT NULL DEFAULT 0 COMMENT '红球命中数要求',
                `blue_hit`      INT            NOT NULL DEFAULT 0 COMMENT '蓝球命中数要求',
                `hit_condition` VARCHAR(100)   NULL COMMENT '命中条件描述（复合条件时使用，如"5红 OR 4红+1蓝"）',
                `prize_type`    VARCHAR(10)    NOT NULL DEFAULT 'FIXED' COMMENT '奖金类型：FIXED固定 POOL浮动',
                `fixed_amount`  DECIMAL(12,2)  NULL COMMENT '固定奖金金额（FIXED类型时使用）',
                `description`   VARCHAR(200)   NULL COMMENT '规则描述',
                `sort_order`    INT            NOT NULL DEFAULT 0 COMMENT '排序字段',
                `created_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                `updated_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
                PRIMARY KEY (`id`),
                UNIQUE KEY `uk_mode_level` (`mode_id`, `win_level`),
                INDEX `idx_mode_id` (`mode_id`),
                INDEX `idx_win_level` (`win_level`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中奖规则表'
            """;
        
        jdbcTemplate.execute(createTableSql);
    }

    /**
     * 插入中奖规则数据
     */
    private void insertWinRuleData() {
        // 双色球中奖规则
        String insertSsqRules = """
            INSERT INTO `t_win_rule` (`mode_id`, `win_level`, `level_name`, `red_hit`, `blue_hit`, `prize_type`, `fixed_amount`, `description`, `sort_order`)
            SELECT id, 1, '一等奖', 6, 1, 'POOL',  NULL,    '6红+1蓝，浮动奖金', 1 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            UNION ALL
            SELECT id, 2, '二等奖', 6, 0, 'POOL',  NULL,    '6红，浮动奖金', 2 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            UNION ALL
            SELECT id, 3, '三等奖', 5, 1, 'FIXED', 3000.00, '5红+1蓝，固定3000元', 3 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            UNION ALL
            SELECT id, 4, '四等奖', 5, 0, 'FIXED', 200.00,  '5红 或 4红+1蓝，固定200元', 4 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            UNION ALL
            SELECT id, 5, '五等奖', 4, 0, 'FIXED', 10.00,   '4红 或 3红+1蓝，固定10元', 5 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            UNION ALL
            SELECT id, 6, '六等奖', 0, 1, 'FIXED', 5.00,    '蓝球命中，固定5元', 6 FROM `t_lottery_mode` WHERE `code` = 'SSQ' AND `deleted` = 0
            """;
        
        jdbcTemplate.execute(insertSsqRules);
        log.info("双色球中奖规则插入完成");
    }

    private void insertTestData(String issueNo, String drawDate, String redNumbers, String blueNumbers) {
        OfficialDrawResult result = OfficialDrawResult.builder()
                .lotteryId(1)
                .issueNo(issueNo)
                .drawDate(LocalDate.parse(drawDate))
                .redNumbers(redNumbers)
                .blueNumbers(blueNumbers)
                .prizePool(new BigDecimal("1000000000.00"))
                .build();
        
        officialDrawResultMapper.insert(result);
        log.debug("插入测试数据: 期号={}, 红球={}, 蓝球={}", issueNo, redNumbers, blueNumbers);
    }
}