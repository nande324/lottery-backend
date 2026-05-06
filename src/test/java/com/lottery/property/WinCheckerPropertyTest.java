package com.lottery.property;

import com.lottery.service.wincheck.SsqWinChecker;
import com.lottery.service.wincheck.WinResult;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性5：中奖核对正确性
 * Feature: lottery-management-system, Property 5: 中奖核对正确性
 * 验证需求：需求 3.7、需求 3.9
 */
class WinCheckerPropertyTest {

    private final SsqWinChecker ssqWinChecker = new SsqWinChecker();

    @Property(tries = 500)
    void winCheckResultShouldMatchIntersection(
            @ForAll("ssqTicketPairs") TicketPair pair
    ) {
        WinResult result = ssqWinChecker.check(
                pair.ticketRed(), pair.ticketBlue(),
                pair.resultRed(), pair.resultBlue()
        );

        // 计算红球命中数（交集大小）
        int expectedRedHit = countIntersection(pair.ticketRed(), pair.resultRed());
        // 计算蓝球命中数
        int expectedBlueHit = countIntersection(pair.ticketBlue(), pair.resultBlue());

        // 验证中奖等级与规则表一致
        Integer winLevel = result.getWinLevel();
        if (result.isWin()) {
            Assertions.assertNotNull(winLevel, "中奖时等级不应为null");
            // 验证等级与命中数的对应关系
            verifyWinLevel(expectedRedHit, expectedBlueHit, winLevel);
        } else {
            // 未中奖时，验证确实不满足任何中奖条件
            Assertions.assertNull(winLevel, "未中奖时等级应为null");
            verifyNoWin(expectedRedHit, expectedBlueHit);
        }
    }

    private void verifyWinLevel(int redHit, int blueHit, int winLevel) {
        // 根据双色球规则验证等级
        if (redHit == 6 && blueHit == 1) {
            Assertions.assertEquals(1, winLevel, "6红+1蓝应为一等奖");
        } else if (redHit == 6 && blueHit == 0) {
            Assertions.assertEquals(2, winLevel, "6红应为二等奖");
        } else if (redHit == 5 && blueHit == 1) {
            Assertions.assertEquals(3, winLevel, "5红+1蓝应为三等奖");
        } else if (redHit == 5 || (redHit == 4 && blueHit == 1)) {
            Assertions.assertEquals(4, winLevel, "5红或4红+1蓝应为四等奖");
        } else if (redHit == 4 || (redHit == 3 && blueHit == 1)) {
            Assertions.assertEquals(5, winLevel, "4红或3红+1蓝应为五等奖");
        } else if (blueHit == 1) {
            Assertions.assertEquals(6, winLevel, "1蓝应为六等奖");
        }
    }

    private void verifyNoWin(int redHit, int blueHit) {
        // 验证确实不满足任何中奖条件
        boolean shouldWin = (redHit == 6 && blueHit == 1)
                || (redHit == 6 && blueHit == 0)
                || (redHit == 5 && blueHit == 1)
                || redHit == 5
                || (redHit == 4 && blueHit == 1)
                || redHit == 4
                || (redHit == 3 && blueHit == 1)
                || blueHit == 1;
        Assertions.assertFalse(shouldWin,
                "红球命中" + redHit + "个，蓝球命中" + blueHit + "个，应该中奖但返回未中奖");
    }

    private int countIntersection(List<Integer> a, List<Integer> b) {
        if (a == null || b == null) return 0;
        Set<Integer> setB = new HashSet<>(b);
        int count = 0;
        for (Integer num : a) {
            if (setB.contains(num)) count++;
        }
        return count;
    }

    record TicketPair(
            List<Integer> ticketRed, List<Integer> ticketBlue,
            List<Integer> resultRed, List<Integer> resultBlue
    ) {}

    @Provide
    Arbitrary<TicketPair> ssqTicketPairs() {
        // 生成双色球投注号码（6红1蓝）和开奖号码（6红1蓝）
        return Combinators.combine(
                ssqNumbers(),  // 投注红球
                ssqBlue(),     // 投注蓝球
                ssqNumbers(),  // 开奖红球
                ssqBlue()      // 开奖蓝球
        ).as(TicketPair::new);
    }

    private Arbitrary<List<Integer>> ssqNumbers() {
        // 从1-33中随机选6个不重复数字
        return Arbitraries.integers().between(1, 33)
                .set().ofSize(6)
                .map(set -> new ArrayList<>(set));
    }

    private Arbitrary<List<Integer>> ssqBlue() {
        // 从1-16中随机选1个蓝球
        return Arbitraries.integers().between(1, 16)
                .map(n -> Collections.singletonList(n));
    }
}
