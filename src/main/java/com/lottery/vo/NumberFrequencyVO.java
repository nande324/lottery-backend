package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号码频率统计 VO
 * 表示某个号码在历史开奖数据中出现的频次
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberFrequencyVO {

    /** 号码 */
    private Integer number;

    /** 出现频次 */
    private int frequency;
}
