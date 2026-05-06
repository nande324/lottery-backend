package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 七乐彩（QLC）中奖核对实现
 *
 * 优先使用传入的规则表（t_win_rule）进行匹配；
 * 若规则表为空，则回退到硬编码规则（向后兼容）。
 *
 * 七乐彩中奖规则（仅红球，无蓝球）：
 * 一等奖：7红（浮动）
 * 二等奖：6红（浮动）
 * 三等奖：5红（固定1000元）
 * 四等奖：4红（固定100元）
 * 五等奖：3红（固定10元）
 */
@Component
public class QlcWinChecker extends AbstractWinChecker {

    @Override
    public WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                           List<Integer> resultRed, List<Integer> resultBlue,
                           List<WinRule> rules) {
        int redHit = countIntersection(ticketRed, resultRed);

        // 优先使用规则表匹配（七乐彩无蓝球，blueHit 固定为 0）
        if (rules != null && !rules.isEmpty()) {
            return matchByRules(redHit, 0, rules);
        }

        // 回退：硬编码规则
        if (redHit == 7) return WinResult.win(1);
        if (redHit == 6) return WinResult.win(2);
        if (redHit == 5) return WinResult.win(3);
        if (redHit == 4) return WinResult.win(4);
        if (redHit == 3) return WinResult.win(5);
        return WinResult.noWin();
    }
}
