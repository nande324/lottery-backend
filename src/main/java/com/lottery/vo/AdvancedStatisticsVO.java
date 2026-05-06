package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 高级统计 VO
 * 包含用户请求的所有高级统计维度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedStatisticsVO {

    // ---- 基础财务 ----
    /** 历史总投入 */
    private BigDecimal totalCost;
    /** 历史总中奖 */
    private BigDecimal totalWin;
    /** 净盈亏 */
    private BigDecimal netProfit;
    /** ROI收益率 = totalWin / totalCost - 1，百分比 */
    private Double roi;

    // ---- 期数/注数 ----
    /** 总期数（不同期号数量） */
    private long totalIssues;
    /** 总注数 */
    private long totalTickets;
    /** 平均每期投入 */
    private BigDecimal avgCostPerIssue;

    // ---- 空窗/连中 ----
    /** 当前空窗期数（最近一次中奖后连续未中奖的期数） */
    private long currentBlankStreak;
    /** 最长连中期数 */
    private long maxWinStreak;
    /** 最长空窗期数 */
    private long maxBlankStreak;

    // ---- 中奖记录 ----
    /** 首次中奖时间 */
    private LocalDateTime firstWinTime;
    /** 首次中奖期号 */
    private String firstWinIssueNo;
    /** 最近中奖时间 */
    private LocalDateTime lastWinTime;
    /** 最近中奖期号 */
    private String lastWinIssueNo;
    /** 最高单笔中奖金额 */
    private BigDecimal maxSingleWin;
    /** 最高中奖等级（数字越小越高） */
    private Integer bestWinLevel;
    /** 最高中奖等级名称 */
    private String bestWinLevelName;
    /** 最大投注金额（单注） */
    private BigDecimal maxBetAmount;

    // ---- 中奖率/间隔 ----
    /** 中奖率（中奖注数/总注数） */
    private Double winRate;
    /** 中奖注数 */
    private long winTickets;
    /** 平均中奖间隔（期） */
    private Double avgWinInterval;
    /** 大额中奖次数（中奖金额 >= 1000元） */
    private long bigWinCount;

    // ---- 各等奖分布 ----
    /** 各等奖中奖次数 Map<等级, 次数> */
    private Map<Integer, Long> winLevelDistribution;
    /** 各等奖名称 Map<等级, 名称> */
    private Map<Integer, String> winLevelNames;

    // ---- 成就徽章 ----
    /** 已解锁的成就徽章列表 */
    private List<AchievementBadge> achievements;

    // ---- 高光时间轴 ----
    /** 高光事件列表（首次中奖、最高中奖、连中记录等） */
    private List<HighlightEvent> highlights;

    // ---- 刮刮乐专属 ----
    /** 刮刮乐总投入 */
    private BigDecimal scratchTotalCost;
    /** 刮刮乐总中奖 */
    private BigDecimal scratchTotalWin;
    /** 刮刮乐净盈亏 */
    private BigDecimal scratchNetProfit;
    /** 刮刮乐总张数 */
    private long scratchTotalQuantity;
    /** 刮刮乐中奖次数 */
    private long scratchWinCount;
    /** 刮刮乐中奖率 */
    private Double scratchWinRate;
    /** 刮刮乐最高单笔中奖 */
    private BigDecimal scratchMaxSingleWin;
    /** 刮刮乐大额中奖次数（≥1000元） */
    private long scratchBigWinCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementBadge {
        private String id;
        private String name;
        private String icon;
        private String description;
        private boolean unlocked;
        private LocalDateTime unlockedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighlightEvent {
        private String type;       // FIRST_WIN / MAX_WIN / WIN_STREAK / etc.
        private String title;
        private String description;
        private LocalDateTime time;
        private String issueNo;
        private BigDecimal amount;
    }
}
