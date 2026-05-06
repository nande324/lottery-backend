package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 中奖号码信息
 * 用于存储用户投注号码中哪些号码中奖了
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WinningNumbersInfo {

    /** 中奖的红球号码列表 */
    private List<Integer> winningRedNumbers;

    /** 中奖的蓝球号码列表 */
    private List<Integer> winningBlueNumbers;

    /** 红球中奖个数 */
    private Integer redCount;

    /** 蓝球中奖个数 */
    private Integer blueCount;
}