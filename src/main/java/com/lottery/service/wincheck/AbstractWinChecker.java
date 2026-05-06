package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WinChecker 抽象基类
 * 提供通用的号码命中数计算和规则表匹配逻辑
 */
public abstract class AbstractWinChecker implements WinChecker {

    /**
     * 计算两个号码列表的交集大小
     */
    protected int countIntersection(List<Integer> a, List<Integer> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<Integer> setB = new HashSet<>(b);
        int count = 0;
        for (Integer num : a) {
            if (setB.contains(num)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据规则表匹配中奖等级（通用逻辑）
     * 遍历规则列表，找到第一条满足命中条件的规则
     *
     * @param redHit  红球命中数
     * @param blueHit 蓝球命中数
     * @param rules   规则列表（按 win_level 升序，即奖级从高到低）
     * @return 匹配的中奖结果，未中奖返回 WinResult.noWin()
     */
    protected WinResult matchByRules(int redHit, int blueHit, List<WinRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return WinResult.noWin();
        }
        for (WinRule rule : rules) {
            if (matchesRule(redHit, blueHit, rule)) {
                if ("POOL".equals(rule.getPrizeType())) {
                    return WinResult.winPool(rule.getWinLevel(), rule.getLevelName());
                } else {
                    return WinResult.winFixed(rule.getWinLevel(), rule.getLevelName(),
                            rule.getFixedAmount() != null ? rule.getFixedAmount() : java.math.BigDecimal.ZERO);
                }
            }
        }
        return WinResult.noWin();
    }

    /**
     * 判断命中数是否满足某条规则
     * 子类可覆盖此方法以实现特殊匹配逻辑（如排列三的精确匹配）
     *
     * @param redHit  红球命中数
     * @param blueHit 蓝球命中数
     * @param rule    规则
     * @return 是否满足
     */
    protected boolean matchesRule(int redHit, int blueHit, WinRule rule) {
        return rule.getRedHit() == redHit && rule.getBlueHit() == blueHit;
    }
}
