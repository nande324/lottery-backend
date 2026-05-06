package com.lottery.property;

import com.lottery.util.NumberRule;
import com.lottery.util.NumberValidator;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;

import java.util.*;

/**
 * 属性2：不合法号码被拒绝
 * Feature: lottery-management-system, Property 2: 不合法号码被拒绝
 * 验证需求：需求 2.5、需求 3.5、需求 3.6
 */
class NumberValidatorPropertyTest {

    private final NumberValidator validator = new NumberValidator();

    @Property(tries = 100)
    void invalidNumbersShouldBeRejected_wrongCount(
            @ForAll("rulesWithWrongCount") InvalidNumberCase testCase
    ) {
        NumberValidator.ValidationResult result = validator.validate(
                testCase.red(), testCase.blue(), testCase.rule());
        Assertions.assertFalse(result.isValid(),
                "号码数量不符时应校验失败，错误信息：" + result.getErrorMessage());
    }

    @Property(tries = 100)
    void invalidNumbersShouldBeRejected_outOfRange(
            @ForAll("rulesWithOutOfRange") InvalidNumberCase testCase
    ) {
        NumberValidator.ValidationResult result = validator.validate(
                testCase.red(), testCase.blue(), testCase.rule());
        Assertions.assertFalse(result.isValid(),
                "号码超出范围时应校验失败，错误信息：" + result.getErrorMessage());
    }

    @Property(tries = 100)
    void invalidNumbersShouldBeRejected_duplicateRed(
            @ForAll("rulesWithDuplicateRed") InvalidNumberCase testCase
    ) {
        NumberValidator.ValidationResult result = validator.validate(
                testCase.red(), testCase.blue(), testCase.rule());
        Assertions.assertFalse(result.isValid(),
                "红球重复时应校验失败，错误信息：" + result.getErrorMessage());
    }

    record InvalidNumberCase(List<Integer> red, List<Integer> blue, NumberRule rule) {}

    @Provide
    Arbitrary<InvalidNumberCase> rulesWithWrongCount() {
        return Combinators.combine(
                Arbitraries.integers().between(2, 6),
                Arbitraries.integers().between(1, 3)
        ).as((redCount, extra) -> {
            NumberRule rule = NumberRule.builder()
                    .redCount(redCount).redMin(1).redMax(33)
                    .blueCount(0).allowRedDuplicate(false).build();
            // 生成数量多于 redCount 的红球（数量不符）
            List<Integer> red = new ArrayList<>();
            for (int i = 1; i <= redCount + extra; i++) red.add(i);
            return new InvalidNumberCase(red, Collections.emptyList(), rule);
        });
    }

    @Provide
    Arbitrary<InvalidNumberCase> rulesWithOutOfRange() {
        return Arbitraries.integers().between(2, 5).map(redCount -> {
            NumberRule rule = NumberRule.builder()
                    .redCount(redCount).redMin(1).redMax(10)
                    .blueCount(0).allowRedDuplicate(false).build();
            // 生成超出范围的红球（最后一个超出 redMax）
            List<Integer> red = new ArrayList<>();
            for (int i = 1; i < redCount; i++) red.add(i);
            red.add(100); // 超出范围
            return new InvalidNumberCase(red, Collections.emptyList(), rule);
        });
    }

    @Provide
    Arbitrary<InvalidNumberCase> rulesWithDuplicateRed() {
        return Arbitraries.integers().between(2, 5).map(redCount -> {
            NumberRule rule = NumberRule.builder()
                    .redCount(redCount).redMin(1).redMax(33)
                    .blueCount(0).allowRedDuplicate(false).build();
            // 生成有重复的红球（全部为1）
            List<Integer> red = new ArrayList<>();
            for (int i = 0; i < redCount; i++) red.add(1);
            return new InvalidNumberCase(red, Collections.emptyList(), rule);
        });
    }
}
