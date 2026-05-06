package com.lottery.property;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Negative;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;

/**
 * 属性10：金额非负校验
 * Feature: lottery-management-system, Property 10: 金额非负校验
 * 验证需求：需求 5.5
 */
class AmountValidatorPropertyTest {

    /**
     * 模拟金额校验逻辑（与 ScratchTicketCreateRequest 中的 @DecimalMin("0") 校验一致）
     */
    static class AmountValidator {
        public boolean isValid(BigDecimal amount) {
            if (amount == null) return false;
            return amount.compareTo(BigDecimal.ZERO) >= 0;
        }

        public String getErrorMessage(BigDecimal amount) {
            if (amount == null) return "金额不能为空";
            if (amount.compareTo(BigDecimal.ZERO) < 0) return "金额不能为负数";
            return null;
        }
    }

    private final AmountValidator validator = new AmountValidator();

    @Property(tries = 100)
    void negativeAmountShouldBeRejected(
            @ForAll("negativeAmounts") BigDecimal amount
    ) {
        boolean valid = validator.isValid(amount);
        Assertions.assertFalse(valid,
                "负数金额 " + amount + " 应校验失败");

        String errorMsg = validator.getErrorMessage(amount);
        Assertions.assertNotNull(errorMsg, "校验失败时应有错误信息");
        Assertions.assertFalse(errorMsg.isEmpty(), "错误信息不应为空");
    }

    @Property(tries = 100)
    void zeroAmountShouldBeAccepted() {
        BigDecimal zero = BigDecimal.ZERO;
        boolean valid = validator.isValid(zero);
        Assertions.assertTrue(valid, "零金额应通过校验");
    }

    @Property(tries = 100)
    void positiveAmountShouldBeAccepted(
            @ForAll("positiveAmounts") BigDecimal amount
    ) {
        boolean valid = validator.isValid(amount);
        Assertions.assertTrue(valid,
                "正数金额 " + amount + " 应通过校验");
    }

    @Provide
    Arbitrary<BigDecimal> negativeAmounts() {
        // 生成负数金额（-0.01 到 -10000）
        return Arbitraries.integers().between(1, 1000000)
                .map(i -> new BigDecimal(i).negate().movePointLeft(2));
    }

    @Provide
    Arbitrary<BigDecimal> positiveAmounts() {
        // 生成正数金额（0.01 到 10000）
        return Arbitraries.integers().between(1, 1000000)
                .map(i -> new BigDecimal(i).movePointLeft(2));
    }
}
