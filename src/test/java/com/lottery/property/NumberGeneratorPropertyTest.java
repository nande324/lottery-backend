package com.lottery.property;

import com.lottery.util.NumberGenerator;
import com.lottery.util.NumberRule;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;

import java.util.*;

/**
 * 属性1：生成号码合法性
 * Feature: lottery-management-system, Property 1: 生成号码合法性
 * 验证需求：需求 2.1
 */
class NumberGeneratorPropertyTest {

    private final NumberGenerator generator = new NumberGenerator();

    @Property(tries = 100)
    void generatedNumbersShouldBeValid(
            @ForAll("validNumberRules") NumberRule rule
    ) {
        Map<String, List<Integer>> result = generator.generate(rule);
        List<Integer> red = result.get("red");
        List<Integer> blue = result.get("blue");

        // 红球数量正确
        Assertions.assertEquals(rule.getRedCount(), red.size(),
                "红球数量应等于 redCount");

        // 每个红球在范围内
        for (Integer num : red) {
            Assertions.assertTrue(num >= rule.getRedMin() && num <= rule.getRedMax(),
                    "红球 " + num + " 超出范围 [" + rule.getRedMin() + ", " + rule.getRedMax() + "]");
        }

        // 非排列三/五时红球无重复
        if (!rule.isAllowRedDuplicate()) {
            Set<Integer> redSet = new HashSet<>(red);
            Assertions.assertEquals(red.size(), redSet.size(), "红球不应有重复");
        }

        // 蓝球校验
        int blueCount = rule.getBlueCount() != null ? rule.getBlueCount() : 0;
        if (blueCount > 0) {
            Assertions.assertNotNull(blue, "蓝球列表不应为null");
            Assertions.assertEquals(blueCount, blue.size(), "蓝球数量应等于 blueCount");
            for (Integer num : blue) {
                Assertions.assertTrue(num >= rule.getBlueMin() && num <= rule.getBlueMax(),
                        "蓝球 " + num + " 超出范围");
            }
        }
    }

    @Provide
    Arbitrary<NumberRule> validNumberRules() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 6),   // redCount
                Arbitraries.integers().between(1, 5),   // redMin
                Arbitraries.integers().between(0, 2)    // blueCount
        ).as((redCount, redMin, blueCount) -> {
            // 确保范围足够容纳 redCount 个不重复号码
            int redMax = redMin + redCount + 10;
            return NumberRule.builder()
                    .redCount(redCount)
                    .redMin(redMin)
                    .redMax(redMax)
                    .blueCount(blueCount)
                    .blueMin(blueCount > 0 ? 1 : null)
                    .blueMax(blueCount > 0 ? 16 : null)
                    .allowRedDuplicate(false)
                    .build();
        });
    }
}
