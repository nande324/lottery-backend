package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 排列三（PL3）中奖核对实现
 *
 * 排列三采用位置敏感的精确匹配，不能用命中数来判断。
 * 规则表中 win_level=1 对应直选（3位全中），win_level=2/3 对应组选。
 * 此处仅实现直选（最常见玩法），组选需前端在保存时标注玩法类型。
 */
@Component
public class Pl3WinChecker extends AbstractWinChecker {

    @Override
    public WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                           List<Integer> resultRed, List<Integer> resultBlue,
                           List<WinRule> rules) {
        if (ticketRed == null || resultRed == null
                || ticketRed.size() < 3 || resultRed.size() < 3) {
            return WinResult.noWin();
        }

        boolean exactMatch = ticketRed.get(0).equals(resultRed.get(0))
                && ticketRed.get(1).equals(resultRed.get(1))
                && ticketRed.get(2).equals(resultRed.get(2));

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
