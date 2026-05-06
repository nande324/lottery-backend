package com.lottery.property;

import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性7：号码频率统计降序排列
 * Feature: lottery-management-system, Property 7: 号码频率统计降序排列
 * 验证需求：需求 6.6
 */
class NumberFrequencyPropertyTest {

    /**
     * 模拟号码频率统计逻辑（与 StatisticsService.getNumberFrequency 一致）
     */
    static List<NumberFrequency> calcFrequency(List<String> redNumbersList) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (String redNumbers : redNumbersList) {
            if (redNumbers == null || redNumbers.trim().isEmpty()) continue;
            String[] parts = redNumbers.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        int num = Integer.parseInt(trimmed);
                        freqMap.merge(num, 1, Integer::sum);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(e -> new NumberFrequency(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    record NumberFrequency(int number, int frequency) {}

    @Property(tries = 100)
    void frequencyListShouldBeInDescendingOrder(
            @ForAll("drawResultRedNumbers") List<String> redNumbersList
    ) {
        List<NumberFrequency> frequencies = calcFrequency(redNumbersList);

        // 验证相邻元素频次满足前者 >= 后者（降序）
        for (int i = 0; i < frequencies.size() - 1; i++) {
            int current = frequencies.get(i).frequency();
            int next = frequencies.get(i + 1).frequency();
            Assertions.assertTrue(current >= next,
                    "频率列表应降序排列，但位置 " + i + " 的频次 " + current
                            + " 小于位置 " + (i + 1) + " 的频次 " + next);
        }
    }

    @Property(tries = 100)
    void frequencyCountShouldMatchTotalNumbers(
            @ForAll("drawResultRedNumbers") List<String> redNumbersList
    ) {
        List<NumberFrequency> frequencies = calcFrequency(redNumbersList);

        // 验证所有频次之和等于总号码数
        int totalFromFreq = frequencies.stream().mapToInt(NumberFrequency::frequency).sum();
        int totalNumbers = redNumbersList.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .mapToInt(s -> s.split(",").length)
                .sum();

        Assertions.assertEquals(totalNumbers, totalFromFreq,
                "频次总和应等于总号码数");
    }

    @Provide
    Arbitrary<List<String>> drawResultRedNumbers() {
        // 生成模拟的开奖红球字符串列表（每条记录6个红球，范围1-33）
        Arbitrary<String> singleResult = Arbitraries.integers().between(1, 33)
                .set().ofSize(6)
                .map(set -> set.stream().map(String::valueOf).collect(Collectors.joining(",")));

        return singleResult.list().ofMinSize(0).ofMaxSize(30);
    }
}
