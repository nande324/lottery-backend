package com.lottery.dto.statistics;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 统计查询请求 DTO
 * 支持按彩票模式、时间范围、中奖状态过滤
 */
@Data
public class StatisticsQueryRequest {

    /**
     * 彩票模式 ID（可选，不传则统计所有模式）
     */
    private Long modeId;

    /**
     * 时间范围枚举值：DAY / WEEK / MONTH / YEAR / CUSTOM
     * 默认 MONTH
     */
    private String timeRange = "MONTH";

    /**
     * 自定义开始日期（timeRange=CUSTOM 时使用）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /**
     * 自定义结束日期（timeRange=CUSTOM 时使用）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /**
     * 中奖状态过滤（可选）：PENDING / NO_WIN / WIN
     */
    private String winStatus;
}
