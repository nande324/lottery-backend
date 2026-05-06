package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 历史最佳中奖记录视图对象
 * 用于"历史最佳"展示区，支持按等级或金额排序的 Top N
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopWinVO {

    /** 摇奖票 ID */
    private Long id;

    /** 彩票模式 ID */
    private Long modeId;

    /** 彩票模式名称 */
    private String modeName;

    /** 期号 */
    private String issueNo;

    /** 红球号码（逗号分隔） */
    private String redNumbers;

    /** 蓝球号码（逗号分隔，可为null） */
    private String blueNumbers;

    /** 中奖等级（1=一等奖） */
    private Integer winLevel;

    /** 中奖等级名称（如"三等奖"） */
    private String levelName;

    /** 中奖金额（元） */
    private BigDecimal winAmount;

    /** 投注时间 */
    private LocalDateTime betTime;

    /** 是否在兑奖期内（投注时间距今 ≤ 60 天） */
    private boolean claimable;

    /** 兑奖截止日期（betTime + 60天） */
    private LocalDateTime claimDeadline;
}
