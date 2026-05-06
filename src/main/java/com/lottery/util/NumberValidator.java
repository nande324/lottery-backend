package com.lottery.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 号码校验器
 * 根据彩票模式的号码规则校验用户输入的号码合法性
 */
@Component
public class NumberValidator {

    /**
     * 校验号码是否符合指定的号码规则
     *
     * @param red  红球号码列表
     * @param blue 蓝球号码列表（无蓝球时传空列表或null）
     * @param rule 号码规则
     * @return 校验结果
     */
    public ValidationResult validate(List<Integer> red, List<Integer> blue, NumberRule rule) {
        // 校验红球数量
        if (red == null || red.size() != rule.getRedCount()) {
            return ValidationResult.fail(
                    String.format("红球数量不正确，应为 %d 个，实际为 %d 个",
                            rule.getRedCount(), red == null ? 0 : red.size())
            );
        }

        // 校验每个红球是否在 [redMin, redMax] 范围内
        for (Integer num : red) {
            if (num == null || num < rule.getRedMin() || num > rule.getRedMax()) {
                return ValidationResult.fail(
                        String.format("红球号码 %s 超出范围 [%d, %d]",
                                num, rule.getRedMin(), rule.getRedMax())
                );
            }
        }

        // 若不允许重复，校验红球无重复
        if (!rule.isAllowRedDuplicate()) {
            Set<Integer> redSet = new HashSet<>(red);
            if (redSet.size() != red.size()) {
                return ValidationResult.fail("红球号码存在重复，不符合该彩票模式的规则");
            }
        }

        // 校验蓝球
        int blueCount = rule.getBlueCount() != null ? rule.getBlueCount() : 0;
        if (blueCount > 0) {
            // 有蓝球：校验蓝球数量和范围
            if (blue == null || blue.size() != blueCount) {
                return ValidationResult.fail(
                        String.format("蓝球数量不正确，应为 %d 个，实际为 %d 个",
                                blueCount, blue == null ? 0 : blue.size())
                );
            }
            for (Integer num : blue) {
                if (num == null || num < rule.getBlueMin() || num > rule.getBlueMax()) {
                    return ValidationResult.fail(
                            String.format("蓝球号码 %s 超出范围 [%d, %d]",
                                    num, rule.getBlueMin(), rule.getBlueMax())
                    );
                }
            }
        } else {
            // 无蓝球：校验 blue 为空或空列表
            if (blue != null && !blue.isEmpty()) {
                return ValidationResult.fail("该彩票模式不支持蓝球，请勿传入蓝球号码");
            }
        }

        return ValidationResult.ok();
    }

    /**
     * 号码校验结果内部静态类
     */
    @Getter
    public static class ValidationResult {

        /** 是否校验通过 */
        private final boolean valid;

        /** 错误信息（校验通过时为null） */
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        /**
         * 创建校验通过的结果
         *
         * @return 校验通过的 ValidationResult
         */
        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        /**
         * 创建校验失败的结果
         *
         * @param errorMessage 错误信息
         * @return 校验失败的 ValidationResult
         */
        public static ValidationResult fail(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }
}
