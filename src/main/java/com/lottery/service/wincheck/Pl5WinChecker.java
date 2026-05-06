package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 排列五（PL5）中奖核对实现
 *
 * 排列五采用位置敏感的精确匹配。
 * 规则表中 win_level=1 对应直选（5位全中）。
 */
@Component
public class Pl5WinChecker extends AbstractWinChecker {

    @Override
    public WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                           List<Integer> resultRed, List<Integer> resultBlue,
                           List<WinRule> rules) {
        if (ticketRed == null || resultRed == null
                || ticketRed.size() < 5 || resultRed.size() < 5) {
            return WinResult.noWin();
        }

        boolean exactMatch = ticketRed.get(0).equals(resultRed.get(0))
                && ticketRed.get(1).equals(resultRed.get(1))
                && ticketRed.get(2).equals(resultRed.get(2))
                && ticketRed.get(3).equals(resultRed.get(3))
                && ticketRed.get(4).equals(resultRed.get(4));

        if (!exactMatch) {
            return WinResult.noWin();
        }

        // 精确匹配成功，从规则表取直选（win_level=1）的奖金信息
        if (rules != null && !rules.isEmpty()) {
            for (WinRule rule : rules) {
                if (rule.getWinLevel() == 1) {
                    if ("POOL".equals(rule.getPrizeType())) {
                        return WinResult.winPool(rule.getWinLevel(), rule.getLevelName());
                    } else {
                        return WinResult.winFixed(rule.getWinLevel(), rule.getLevelName(),
                                rule.getFixedAmount() != null ? rule.getFixedAmount() : java.math.BigDecimal.ZERO);
                    }
                }
            }
        }

        // 回退：硬编码
        return WinResult.win(1);
    }
}
