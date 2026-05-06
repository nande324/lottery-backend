package com.lottery.service.wincheck;

import com.lottery.entity.WinRule;

import java.util.List;

/**
 * 中奖核对接口
 * 各彩票模式实现此接口，提供对应的中奖规则判断逻辑
 */
public interface WinChecker {

    /**
     * 核对投注号码与开奖号码，根据规则表返回中奖结果
     * 规则从 t_win_rule 查询传入，不硬编码奖级逻辑
     *
     * @param ticketRed  投注红球号码列表
     * @param ticketBlue 投注蓝球号码列表（无蓝球时传空列表或null）
     * @param resultRed  开奖红球号码列表
     * @param resultBlue 开奖蓝球号码列表（无蓝球时传空列表或null）
     * @param rules      该模式的中奖规则列表（从 t_win_rule 查询，按 win_level 升序）
     * @return 中奖核对结果
     */
    WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                    List<Integer> resultRed, List<Integer> resultBlue,
                    List<WinRule> rules);

    /**
     * 向后兼容方法（不传规则，使用硬编码逻辑）
     * 默认实现调用带规则的方法，传入空列表（各实现类可覆盖）
     *
     * @deprecated 请使用带 rules 参数的方法
     */
    @Deprecated
    default WinResult check(List<Integer> ticketRed, List<Integer> ticketBlue,
                            List<Integer> resultRed, List<Integer> resultBlue) {
        return check(ticketRed, ticketBlue, resultRed, resultBlue, java.util.Collections.emptyList());
    }
}
