package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 双色球（SSQ）中奖核对实现
 *
 * 优先使用传入的规则表（t_win_rule）进行匹配；
 * 若规则表为空，则回退到硬编码规则（向后兼容）。
 *
 * 双色球中奖规则：
 * 一等奖：6红 + 1蓝  → 浮动（奖池相关）
 * 二等奖：6红        → 浮动
 * 三等奖：5红 + 1蓝  → 固定 3000 元
 * 四等奖：5红 或 4红+1蓝 → 固定 200 元
 * 五等奖：4红 或 3红+1蓝 → 固定 10 元
 * 六等奖：蓝球命中 1  → 固定 5 元
 */
@Component
public class SsqWinChecker extends AbstractWinChecker {

    private static final BigDecimal PRIZE_3 = new BigDecimal("3000");
    private static final BigDecimal PRIZE_4 = new BigDecimal("200");
    private static final BigDecimal PRIZE_5 = new BigDecimal("10");
    private static final BigDecimal PRIZE_6 = new BigDecimal("5");

    @Override
    public WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                           List<Integer> resultRed, List<Integer> resultBlue,
                           List<WinRule> rules) {
        int redHit = countIntersection(ticketRed, resultRed);
        int blueHit = countIntersection(ticketBlue, resultBlue);

        // 优先使用规则表匹配
        if (rules != null && !rules.isEmpty()) {
            return matchSsqByRules(redHit, blueHit, rules);
        }

        // 回退：硬编码规则（向后兼容）
        return matchSsqHardcoded(redHit, blueHit);
    }

    /**
     * 基于规则表的双色球匹配
     * 双色球部分等级有复合条件（如四等奖：5红 OR 4红+1蓝），
     * 需要特殊处理，不能简单用 redHit==x && blueHit==y 匹配
     */
    private WinResult matchSsqByRules(int redHit, int blueHit, List<WinRule> rules) {
        // 一等奖：6红+1蓝
        if (redHit == 6 && blueHit == 1) return findRule(rules, 1);
        // 二等奖：6红
        if (redHit == 6 && blueHit == 0) return findRule(rules, 2);
        // 三等奖：5红+1蓝
        if (redHit == 5 && blueHit == 1) return findRule(rules, 3);
        // 四等奖：5红 或 4红+1蓝
        if (redHit == 5 || (redHit == 4 && blueHit == 1)) return findRule(rules, 4);
        // 五等奖：4红 或 3红+1蓝
        if (redHit == 4 || (redHit == 3 && blueHit == 1)) return findRule(rules, 5);
        // 六等奖：蓝球命中1（任意红球数）
        if (blueHit == 1) return findRule(rules, 6);
        return WinResult.noWin();
    }

    /**
     * 从规则列表中找到指定等级的规则并构建 WinResult
     */
    private WinResult findRule(List<WinRule> rules, int level) {
        for (WinRule rule : rules) {
            if (rule.getWinLevel() == level) {
                if ("POOL".equals(rule.getPrizeType())) {
                    return WinResult.winPool(rule.getWinLevel(), rule.getLevelName());
                } else {
                    return WinResult.winFixed(rule.getWinLevel(), rule.getLevelName(),
                            rule.getFixedAmount() != null ? rule.getFixedAmount() : BigDecimal.ZERO);
                }
            }
        }
        // 规则表中没有该等级，回退到硬编码
        return matchSsqHardcoded(0, 0);
    }

    /**
     * 硬编码规则（向后兼容，规则表为空时使用）
     */
    private WinResult matchSsqHardcoded(int redHit, int blueHit) {
        if (redHit == 6 && blueHit == 1) return WinResult.win(1);
        if (redHit == 6 && blueHit == 0) return WinResult.win(2);
        if (redHit == 5 && blueHit == 1) return WinResult.win(3, PRIZE_3);
        if (redHit == 5 || (redHit == 4 && blueHit == 1)) return WinResult.win(4, PRIZE_4);
        if (redHit == 4 || (redHit == 3 && blueHit == 1)) return WinResult.win(5, PRIZE_5);
        if (blueHit == 1) return WinResult.win(6, PRIZE_6);
        return WinResult.noWin();
    }
}
