package com.lottery.util;

import com.lottery.entity.LotteryMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号码规则值对象
 * 封装各彩票模式的选号规则，用于号码生成与校验
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NumberRule {

    /** 红球数量 */
    private Integer redCount;

    /** 红球最小值 */
    private Integer redMin;

    /** 红球最大值 */
    private Integer redMax;

    /** 蓝球数量（0表示无蓝球） */
    private Integer blueCount;

    /** 蓝球最小值（可为null） */
    private Integer blueMin;

    /** 蓝球最大值（可为null） */
    private Integer blueMax;

    /** 是否允许红球重复（排列三/五为true） */
    private boolean allowRedDuplicate;

    /**
     * 从 LotteryMode 实体构建 NumberRule 的静态工厂方法
     *
     * @param mode 彩票模式实体
     * @return 对应的号码规则
     */
    public static NumberRule fromLotteryMode(LotteryMode mode) {
        // PL3/PL5（排列三/排列五）允许号码重复
        boolean allowDuplicate = "PL3".equals(mode.getCode()) || "PL5".equals(mode.getCode());
        return NumberRule.builder()
                .redCount(mode.getRedCount() != null ? mode.getRedCount() : 0)
                .redMin(mode.getRedMin() != null ? mode.getRedMin() : 0)
                .redMax(mode.getRedMax() != null ? mode.getRedMax() : 9)
                .blueCount(mode.getBlueCount() != null ? mode.getBlueCount() : 0)
                .blueMin(mode.getBlueMin())
                .blueMax(mode.getBlueMax())
                .allowRedDuplicate(allowDuplicate)
                .build();
    }
}
