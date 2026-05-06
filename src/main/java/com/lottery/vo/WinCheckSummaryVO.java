package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 中奖核对汇总结果视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WinCheckSummaryVO {

    /** 参与核对的总注数 */
    private int totalTickets;

    /** 中奖注数 */
    private int winTickets;

    /** 各等级中奖数量（key=中奖等级，value=该等级中奖注数） */
    private Map<Integer, Integer> winLevelCounts;
}
