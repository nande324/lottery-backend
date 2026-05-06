package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 统计概览 VO
 * 包含总消费、总中奖、净盈亏、总注数、中奖注数、中奖率
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsOverviewVO {

    /** 总消费金额（元） */
    private BigDecimal totalCost;

    /** 总中奖金额（元） */
    private BigDecimal totalWin;

    /** 净盈亏 = totalWin - totalCost */
    private BigDecimal netProfit;

    /** 总注数 */
    private long totalTickets;

    /** 中奖注数 */
    private long winTickets;

    /** 中奖率 = winTickets / totalTickets */
    private double winRate;
}
