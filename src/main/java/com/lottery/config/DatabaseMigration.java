package com.lottery.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库迁移组件
 * 在应用启动时执行必要的数据库结构更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查是否需要添加 draw_time 字段
            addDrawTimeColumnIfNotExists();
            
            // 检查是否需要添加 is_claimed 字段
            addIsClaimedColumnIfNotExists();
            
            // 检查是否需要添加 winning_numbers 字段
            addWinningNumbersColumnIfNotExists();

            // 检查是否需要添加 unit_price 字段（刮刮乐单张面额）
            addScratchUnitPriceColumnIfNotExists();

            // 确保 unit_price 字段为 NOT NULL
            ensureScratchUnitPriceNotNull();

            // 检查是否需要添加 quantity 字段（刮刮乐购买张数）
            addScratchQuantityColumnIfNotExists();
            
            log.info("数据库迁移检查完成");
        } catch (Exception e) {
            log.error("数据库迁移失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    private void addDrawTimeColumnIfNotExists() {
        try {
            // 检查字段是否存在
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_draw_ticket' AND COLUMN_NAME = 'draw_time'";
            
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                // 字段不存在，添加字段
                String alterSql = "ALTER TABLE t_draw_ticket " +
                        "ADD COLUMN draw_time DATETIME NULL COMMENT '开奖时间（匹配开奖结果时自动保存）' " +
                        "AFTER win_amount";
                
                jdbcTemplate.execute(alterSql);
                log.info("成功添加 draw_time 字段到 t_draw_ticket 表");
            } else {
                log.debug("draw_time 字段已存在，跳过添加");
            }
        } catch (Exception e) {
            log.warn("添加 draw_time 字段失败: {}", e.getMessage());
        }
    }

    private void addIsClaimedColumnIfNotExists() {
        try {
            // 检查字段是否存在
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_draw_ticket' AND COLUMN_NAME = 'is_claimed'";
            
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                // 字段不存在，添加字段
                String alterSql = "ALTER TABLE t_draw_ticket " +
                        "ADD COLUMN is_claimed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已兑奖：0未兑奖 1已兑奖' " +
                        "AFTER draw_time";
                
                jdbcTemplate.execute(alterSql);
                log.info("成功添加 is_claimed 字段到 t_draw_ticket 表");
            } else {
                log.debug("is_claimed 字段已存在，跳过添加");
            }
        } catch (Exception e) {
            log.warn("添加 is_claimed 字段失败: {}", e.getMessage());
        }
    }

    private void addWinningNumbersColumnIfNotExists() {
        try {
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_draw_ticket' AND COLUMN_NAME = 'winning_numbers'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count == null || count == 0) {
                String alterSql = "ALTER TABLE t_draw_ticket " +
                        "ADD COLUMN winning_numbers TEXT NULL COMMENT '中奖号码（JSON格式存储红球和蓝球的中奖情况）' " +
                        "AFTER is_claimed";
                jdbcTemplate.execute(alterSql);
                log.info("成功添加 winning_numbers 字段到 t_draw_ticket 表");
            } else {
                log.debug("winning_numbers 字段已存在，跳过添加");
            }
        } catch (Exception e) {
            log.warn("添加 winning_numbers 字段失败: {}", e.getMessage());
        }
    }

    private void addScratchUnitPriceColumnIfNotExists() {
        try {
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_scratch_ticket' AND COLUMN_NAME = 'unit_price'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count == null || count == 0) {
                String alterSql = "ALTER TABLE t_scratch_ticket " +
                        "ADD COLUMN unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单张面额（元）' " +
                        "AFTER scratch_type";
                jdbcTemplate.execute(alterSql);
                log.info("成功添加 unit_price 字段到 t_scratch_ticket 表");
            } else {
                log.debug("unit_price 字段已存在，跳过添加");
            }
        } catch (Exception e) {
            log.warn("添加 unit_price 字段失败: {}", e.getMessage());
        }
    }

    private void ensureScratchUnitPriceNotNull() {
        try {
            String checkSql = "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_scratch_ticket' AND COLUMN_NAME = 'unit_price'";
            String nullable = jdbcTemplate.queryForObject(checkSql, String.class);
            if ("YES".equals(nullable)) {
                jdbcTemplate.execute("UPDATE t_scratch_ticket SET unit_price = 0 WHERE unit_price IS NULL");
                jdbcTemplate.execute("ALTER TABLE t_scratch_ticket " +
                        "MODIFY COLUMN unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单张面额（元）'");
                log.info("已将 unit_price 字段修改为 NOT NULL");
            }
        } catch (Exception e) {
            log.warn("修改 unit_price 字段约束失败: {}", e.getMessage());
        }
    }

    private void addScratchQuantityColumnIfNotExists() {
        try {
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_scratch_ticket' AND COLUMN_NAME = 'quantity'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE t_scratch_ticket " +
                        "ADD COLUMN quantity INT NOT NULL DEFAULT 1 COMMENT '购买张数' " +
                        "AFTER unit_price");
                // 根据现有 cost_amount / unit_price 反推张数
                jdbcTemplate.execute(
                        "UPDATE t_scratch_ticket SET quantity = " +
                        "CASE WHEN unit_price > 0 THEN GREATEST(1, ROUND(cost_amount / unit_price)) ELSE 1 END");
                log.info("成功添加 quantity 字段到 t_scratch_ticket 表");
            } else {
                log.debug("quantity 字段已存在，跳过添加");
            }
        } catch (Exception e) {
            log.warn("添加 quantity 字段失败: {}", e.getMessage());
        }
    }
}