package com.lottery.service.wincheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 中奖核对结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinResult {

    /** 是否中奖 */
    private boolean win;

    /** 中奖等级（1=一等奖，数字越小等级越高；null 表示未中奖） */
    private Integer winLevel;

    /** 等级名称（如"三等奖"；null 表示未中奖） */
    private String levelName;

    /** 中奖金额（FIXED 类型自动填入，POOL 类型为 0 待用户手动填写） */
    private BigDecimal winAmount;

    /** 奖金类型：FIXED / POOL（null 表示未中奖） */
    private String prizeType;

    /**
     * 创建未中奖结果
     */
    public static WinResult noWin() {
        return new WinResult(false, null, null, BigDecimal.ZERO, null);
    }

    /**
     * 创建中奖结果（浮动奖金，金额为 0 待用户手动填写）
     *
     * @param level     中奖等级
     * @param levelName 等级名称
     * @return 中奖结果
     */
    public static WinResult winPool(int level, String levelName) {
        return new WinResult(true, level, levelName, BigDecimal.ZERO, "POOL");
    }

    /**
     * 创建中奖结果（固定金额）
     *
     * @param level     中奖等级
     * @param levelName 等级名称
     * @param amount    固定中奖金额
     * @return 中奖结果
     */
    public static WinResult winFixed(int level, String levelName, BigDecimal amount) {
        return new WinResult(true, level, levelName, amount, "FIXED");
    }

    // ---- 向后兼容的工厂方法（供测试使用）----

    /**
     * 创建中奖结果（金额为 0，适用于浮动奖金）
     *
     * @param level 中奖等级
     * @return 中奖结果
     */
    public static WinResult win(int level) {
        return new WinResult(true, level, null, BigDecimal.ZERO, "POOL");
    }

    /**
     * 创建中奖结果（带固定金额）
     *
     * @param level  中奖等级
     * @param amount 固定中奖金额
     * @return 中奖结果
     */
    public static WinResult win(int level, BigDecimal amount) {
        return new WinResult(true, level, null, amount, "FIXED");
    }
}
