package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 趋势数据点 VO
 * 表示某个时间段内的消费与中奖金额
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO {

    /** 时间段标签，如 "2024-01"、"2024-01-15" */
    private String period;

    /** 该时间段消费金额（元） */
    private BigDecimal cost;

    /** 该时间段中奖金额（元） */
    private BigDecimal win;
}
