package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 大乐透（DLT）中奖核对实现
 *
 * 优先使用传入的规则表（t_win_rule）进行匹配；
 * 若规则表为空，则回退到硬编码规则（向后兼容）。
 *
 * 大乐透中奖规则：
 * 一等奖：5红 + 2蓝（浮动）
 * 二等奖：5红 + 1蓝（浮动）
 * 三等奖：5红（固定10000元）
 * 四等奖：4红 + 2蓝（固定3000元）
 * 五等奖：4红 + 1蓝（固定300元）
 * 六等奖：3红 + 2蓝（固定200元）
 * 七等奖：4红（固定100元）
 * 八等奖：3红 + 1蓝（固定15元）
 * 九等奖：2红 + 2蓝（固定15元）
 * 十等奖：3红（固定5元）
 * 十一等奖：1红 + 2蓝（固定5元）
 * 十二等奖：2蓝（固定5元）
 */
@Component
public class DltWinChecker extends AbstractWinChecker {

    @Override
    public WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                           List<Integer> resultRed, List<Integer> resultBlue,
                           List<WinRule> rules) {
        int redHit = countIntersection(ticketRed, resultRed);
        int blueHit = countIntersection(ticketBlue, resultBlue);

        // 优先使用规则表匹配
        if (rules != null && !rules.isEmpty()) {
            return matchDltByRules(redHit, blueHit, rules);
        }

        // 回退：硬编码规则
        return matchDltHardcoded(redHit, blueHit);
    }

    /**
     * 基于规则表的大乐透匹配
     * 大乐透部分等级有复合条件，需要特殊处理
     */
    private WinResult matchDltByRules(int redHit, int blueHit, List<WinRule> rules) {
        // 按优先级从高到低匹配（规则表已按 win_level 升序排列）
        if (redHit == 5 && blueHit == 2) return findRule(rules, 1);
        if (redHit == 5 && blueHit == 1) return findRule(rules, 2);
        if (redHit == 5 && blueHit == 0) return findRule(rules, 3);
        if (redHit == 4 && blueHit == 2) return findRule(rules, 4);
        if (redHit == 4 && blueHit == 1) return findRule(rules, 5);
        if (redHit == 3 && blueHit == 2) return findRule(rules, 6);
        if (redHit == 4 && blueHit == 0) return findRule(rules, 7);
        if (redHit == 3 && blueHit == 1) return findRule(rules, 8);
        if (redHit == 2 && blueHit == 2) return findRule(rules, 9);
        if (redHit == 3 && blueHit == 0) return findRule(rules, 10);
        if (redHit == 1 && blueHit == 2) return findRule(rules, 11);
        if (redHit == 0 && blueHit == 2) return findRule(rules, 12);
        return WinResult.noWin();
    }

    private WinResult findRule(List<WinRule> rules, int level) {
        for (WinRule rule : rules) {
            if (rule.getWinLevel() == level) {
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

    private WinResult matchDltHardcoded(int redHit, int blueHit) {
        if (redHit == 5 && blueHit == 2) return WinResult.win(1);
        if (redHit == 5 && blueHit == 1) return WinResult.win(2);
        if (redHit == 5 && blueHit == 0) return WinResult.win(3);
        if (redHit == 4 && blueHit == 2) return WinResult.win(4);
        if (redHit == 4 && blueHit == 1) return WinResult.win(5);
        if (redHit == 3 && blueHit == 2) return WinResult.win(6);
        if (redHit == 4 && blueHit == 0) return WinResult.win(7);
        if (redHit == 3 && blueHit == 1) return WinResult.win(8);
        if (redHit == 2 && blueHit == 2) return WinResult.win(9);
        if (redHit == 3 && blueHit == 0) return WinResult.win(10);
        if (redHit == 1 && blueHit == 2) return WinResult.win(11);
        if (redHit == 0 && blueHit == 2) return WinResult.win(12);
        return WinResult.noWin();
    }
}
