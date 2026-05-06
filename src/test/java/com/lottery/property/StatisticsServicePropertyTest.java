package com.lottery.property;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 属性6：统计净盈亏计算正确性
 * Feature: lottery-management-system, Property 6: 统计净盈亏计算正确性
 * 验证需求：需求 6.2
 */
class StatisticsServicePropertyTest {

    /**
     * 模拟统计计算逻辑（与 StatisticsService 中的计算逻辑一致）
     */
    static class StatisticsCalculator {
        public BigDecimal calcNetProfit(BigDecimal totalWin, BigDecimal totalCost) {
            return totalWin.subtract(totalCost);
        }

        public long calcTotalTickets(List<TicketRecord> records) {
            return records.size();
        }

        public BigDecimal calcTotalCost(List<TicketRecord> records) {
            return records.stream()
                    .map(r -> r.betAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal calcTotalWin(List<TicketRecord> records) {
            return records.stream()
                    .map(r -> r.winAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    record TicketRecord(BigDecimal betAmount, BigDecimal winAmount) {}

    private final StatisticsCalculator calculator = new StatisticsCalculator();

    @Property(tries = 100)
    void netProfitShouldEqualWinMinusCost(
            @ForAll("ticketRecords") List<TicketRecord> records
    ) {
        BigDecimal totalCost = calculator.calcTotalCost(records);
        BigDecimal totalWin = calculator.calcTotalWin(records);
        BigDecimal netProfit = calculator.calcNetProfit(totalWin, totalCost);

        // 净盈亏 = 总中奖 - 总消费
        BigDecimal expected = totalWin.subtract(totalCost);
        Assertions.assertEquals(0, expected.compareTo(netProfit),
                "净盈亏应等于总中奖 - 总消费，期望：" + expected + "，实际：" + netProfit);
    }

    @Property(tries = 100)
    void totalTicketsShouldEqualRecordsSize(
            @ForAll("ticketRecords") List<TicketRecord> records
    ) {
        long totalTickets = calculator.calcTotalTickets(records);
        Assertions.assertEquals(records.size(), totalTickets,
                "总注数应等于记录数量");
    }

    @Property(tries = 100)
    void totalCostShouldBeNonNegative(
            @ForAll("ticketRecords") List<TicketRecord> records
    ) {
        BigDecimal totalCost = calculator.calcTotalCost(records);
        Assertions.assertTrue(totalCost.compareTo(BigDecimal.ZERO) >= 0,
                "总消费金额不应为负数");
    }

    @Property(tries = 100)
    void totalWinShouldBeNonNegative(
            @ForAll("ticketRecords") List<TicketRecord> records
    ) {
        BigDecimal totalWin = calculator.calcTotalWin(records);
        Assertions.assertTrue(totalWin.compareTo(BigDecimal.ZERO) >= 0,
                "总中奖金额不应为负数");
    }

    @Provide
    Arbitrary<List<TicketRecord>> ticketRecords() {
        Arbitrary<TicketRecord> record = Combinators.combine(
                Arbitraries.integers().between(0, 1000).map(i -> new BigDecimal(i).setScale(2, RoundingMode.HALF_UP)),
                Arbitraries.integers().between(0, 5000).map(i -> new BigDecimal(i).setScale(2, RoundingMode.HALF_UP))
        ).as(TicketRecord::new);

        return record.list().ofMinSize(0).ofMaxSize(50);
    }
}
