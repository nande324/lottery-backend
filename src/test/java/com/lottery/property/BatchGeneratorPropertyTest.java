package com.lottery.property;

import com.lottery.util.NumberGenerator;
import com.lottery.util.NumberRule;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性3：批量生成号码互不相同
 * Feature: lottery-management-system, Property 3: 批量生成号码互不相同
 * 验证需求：需求 2.2
 */
class BatchGeneratorPropertyTest {

    private final NumberGenerator generator = new NumberGenerator();

    @Property(tries = 100)
    void batchGeneratedNumbersShouldBeUnique(
            @ForAll @IntRange(min = 1, max = 20) int count
    ) {
        // 使用双色球规则（范围足够大，可生成足够多的不重复号码）
        NumberRule rule = NumberRule.builder()
                .redCount(6).redMin(1).redMax(33)
                .blueCount(1).blueMin(1).blueMax(16)
                .allowRedDuplicate(false)
                .build();

        List<Map<String, List<Integer>>> results = generator.generateBatch(rule, count);

        // 序列化每注号码为字符串，检查唯一性
        Set<String> uniqueKeys = new HashSet<>();
        for (Map<String, List<Integer>> ticket : results) {
            List<Integer> red = ticket.getOrDefault("red", Collections.emptyList());
            List<Integer> blue = ticket.getOrDefault("blue", Collections.emptyList());
            String key = red.stream().map(String::valueOf).collect(Collectors.joining(","))
                    + "+" + blue.stream().map(String::valueOf).collect(Collectors.joining(","));
            uniqueKeys.add(key);
        }

        Assertions.assertEquals(results.size(), uniqueKeys.size(),
                "批量生成的 " + results.size() + " 注号码中存在重复");
    }
}
