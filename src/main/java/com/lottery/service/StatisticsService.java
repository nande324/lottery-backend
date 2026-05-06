package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.dto.statistics.StatisticsQueryRequest;
import com.lottery.entity.DrawResult;
import com.lottery.entity.DrawTicket;
import com.lottery.entity.LotteryMode;
import com.lottery.entity.WinRule;
import com.lottery.mapper.DrawResultMapper;
import com.lottery.mapper.DrawTicketMapper;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.mapper.ScratchTicketMapper;
import com.lottery.vo.AdvancedStatisticsVO;
import com.lottery.vo.ModeDistributionVO;
import com.lottery.vo.NumberFrequencyVO;
import com.lottery.vo.StatisticsOverviewVO;
import com.lottery.vo.TrendPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析 Service
 * 提供综合概览、趋势、模式占比、号码频率等统计功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DrawTicketMapper drawTicketMapper;
    private final ScratchTicketMapper scratchTicketMapper;
    private final DrawResultMapper drawResultMapper;
    private final LotteryModeMapper lotteryModeMapper;
    private final WinRuleService winRuleService;

    // -------------------------------------------------------------------------
    // 日期范围解析
    // -------------------------------------------------------------------------

    /**
     * 根据 timeRange 枚举值计算开始日期
     */
    private LocalDate resolveStartDate(StatisticsQueryRequest query) {
        if ("CUSTOM".equalsIgnoreCase(query.getTimeRange())) {
            return query.getStartDate();
        }
        LocalDate today = LocalDate.now();
        switch (query.getTimeRange() == null ? "MONTH" : query.getTimeRange().toUpperCase()) {
            case "ALL":   return null;  // 全部时间，不限制开始日期
            case "DAY":   return today;
            case "WEEK":  return today.with(java.time.DayOfWeek.MONDAY);  // 本周一
            case "YEAR":  return today.withDayOfYear(1);                  // 本年1月1日
            case "MONTH":
            default:      return today.withDayOfMonth(1);                 // 本月1日
        }
    }

    /**
     * 根据 timeRange 枚举值计算结束日期
     */
    private LocalDate resolveEndDate(StatisticsQueryRequest query) {
        if ("CUSTOM".equalsIgnoreCase(query.getTimeRange())) {
            return query.getEndDate();
        }
        if ("ALL".equalsIgnoreCase(query.getTimeRange())) {
            return null;  // 全部时间，不限制结束日期
        }
        return LocalDate.now();
    }

    /**
     * 根据 timeRange 确定 SQL 分组方式
     */
    private String resolveGroupBy(StatisticsQueryRequest query) {
        if (query.getTimeRange() == null) {
            return "MONTH";
        }
        switch (query.getTimeRange().toUpperCase()) {
            case "DAY":  return "DAY";
            case "WEEK": return "DAY";   // 本周按天分组
            case "YEAR": return "MONTH"; // 本年按月分组
            case "ALL":  return "YEAR";  // 全部按年分组
            default:     return "MONTH";
        }
    }

    // -------------------------------------------------------------------------
    // 工具方法：安全转换 Map 中的数值
    // -------------------------------------------------------------------------

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    // -------------------------------------------------------------------------
    // 1. 综合统计概览
    // -------------------------------------------------------------------------

    /**
     * 获取综合统计概览
     * 合并摇奖票和刮刮乐数据，计算净盈亏和中奖率
     *
     * @param userId 当前用户ID
     * @param query  查询条件
     * @return StatisticsOverviewVO
     */
    public StatisticsOverviewVO getOverview(Long userId, StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);

        // 摇奖票统计
        Map<String, Object> drawStats = drawTicketMapper.getOverviewStats(
                userId, query.getModeId(), startDate, endDate);

        BigDecimal drawCost    = toBigDecimal(drawStats.get("totalCost"));
        BigDecimal drawWin     = toBigDecimal(drawStats.get("totalWin"));
        long drawTotal         = toLong(drawStats.get("totalTickets"));
        long drawWinCount      = toLong(drawStats.get("winTickets"));

        // 刮刮乐统计（仅在未指定 modeId 时合并，或 modeId 对应刮刮乐模式时）
        BigDecimal scratchCost  = BigDecimal.ZERO;
        BigDecimal scratchWin   = BigDecimal.ZERO;
        long scratchTotal       = 0L;
        long scratchWinCount    = 0L;

        if (query.getModeId() == null) {
            // 未指定模式时，合并刮刮乐数据
            Map<String, Object> scratchStats = scratchTicketMapper.getOverviewStats(
                    userId, startDate, endDate);
            scratchCost     = toBigDecimal(scratchStats.get("totalCost"));
            scratchWin      = toBigDecimal(scratchStats.get("totalWin"));
            scratchTotal    = toLong(scratchStats.get("totalTickets"));
            scratchWinCount = toLong(scratchStats.get("winTickets"));
        }

        // 合并
        BigDecimal totalCost    = drawCost.add(scratchCost);
        BigDecimal totalWin     = drawWin.add(scratchWin);
        BigDecimal netProfit    = totalWin.subtract(totalCost);
        long totalTickets       = drawTotal + scratchTotal;
        long winTickets         = drawWinCount + scratchWinCount;
        double winRate          = totalTickets == 0 ? 0.0
                : BigDecimal.valueOf(winTickets)
                    .divide(BigDecimal.valueOf(totalTickets), 4, RoundingMode.HALF_UP)
                    .doubleValue();

        return StatisticsOverviewVO.builder()
                .totalCost(totalCost)
                .totalWin(totalWin)
                .netProfit(netProfit)
                .totalTickets(totalTickets)
                .winTickets(winTickets)
                .winRate(winRate)
                .build();
    }

    // -------------------------------------------------------------------------
    // 2. 消费/中奖趋势
    // -------------------------------------------------------------------------

    /**
     * 获取消费与中奖趋势数据（摇奖票 + 刮刮乐合并）
     * 未指定 modeId 时，将刮刮乐数据按时间段叠加到摇奖票数据上
     *
     * @param userId 当前用户ID
     * @param query  查询条件
     * @return 趋势数据点列表（按时间升序）
     */
    public List<TrendPointVO> getTrend(Long userId, StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);
        String groupBy      = resolveGroupBy(query);

        // 摇奖票趋势
        List<Map<String, Object>> drawRows = drawTicketMapper.getTrendStats(
                userId, query.getModeId(), groupBy, startDate, endDate);

        // 合并刮刮乐（仅在未指定 modeId 时）
        Map<String, BigDecimal[]> merged = new java.util.LinkedHashMap<>();

        if (drawRows != null) {
            for (Map<String, Object> row : drawRows) {
                String period = row.get("period") == null ? "" : row.get("period").toString();
                merged.put(period, new BigDecimal[]{
                        toBigDecimal(row.get("cost")),
                        toBigDecimal(row.get("win"))
                });
            }
        }

        if (query.getModeId() == null) {
            List<Map<String, Object>> scratchRows = scratchTicketMapper.getTrendStats(
                    userId, groupBy, startDate, endDate);
            if (scratchRows != null) {
                for (Map<String, Object> row : scratchRows) {
                    String period = row.get("period") == null ? "" : row.get("period").toString();
                    BigDecimal sCost = toBigDecimal(row.get("cost"));
                    BigDecimal sWin  = toBigDecimal(row.get("win"));
                    merged.merge(period,
                            new BigDecimal[]{sCost, sWin},
                            (existing, incoming) -> new BigDecimal[]{
                                    existing[0].add(incoming[0]),
                                    existing[1].add(incoming[1])
                            });
                }
            }
        }

        if (merged.isEmpty()) {
            return Collections.emptyList();
        }

        return merged.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> TrendPointVO.builder()
                        .period(e.getKey())
                        .cost(e.getValue()[0])
                        .win(e.getValue()[1])
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 3. 彩票模式消费占比
    // -------------------------------------------------------------------------

    /**
     * 获取各彩票模式消费占比
     *
     * @param userId 当前用户ID
     * @param query  查询条件
     * @return 模式占比列表
     */
    public List<ModeDistributionVO> getModeDistribution(Long userId, StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);

        List<Map<String, Object>> rows = drawTicketMapper.getModeDistribution(
                userId, startDate, endDate);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算总消费（用于百分比）
        BigDecimal grandTotal = rows.stream()
                .map(r -> toBigDecimal(r.get("totalCost")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 批量查询模式名称
        Set<Long> modeIds = rows.stream()
                .map(r -> toLong(r.get("modeId")))
                .collect(Collectors.toSet());

        Map<Long, String> modeNameMap = new HashMap<>();
        if (!modeIds.isEmpty()) {
            List<LotteryMode> modes = lotteryModeMapper.selectList(
                    new LambdaQueryWrapper<LotteryMode>()
                            .in(LotteryMode::getId, modeIds));
            modes.forEach(m -> modeNameMap.put(m.getId(), m.getName()));
        }

        return rows.stream().map(row -> {
            Long modeId       = toLong(row.get("modeId"));
            BigDecimal cost   = toBigDecimal(row.get("totalCost"));
            double percentage = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : cost.divide(grandTotal, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .doubleValue();
            return ModeDistributionVO.builder()
                    .modeId(modeId)
                    .modeName(modeNameMap.getOrDefault(modeId, "未知模式"))
                    .totalCost(cost)
                    .percentage(percentage)
                    .build();
        }).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 4. 号码频率统计
    // -------------------------------------------------------------------------

    /**
     * 获取号码频率统计（Java 层解析 red_numbers 字段）
     * 统计该用户该模式历史开奖数据中各号码出现频次，按频次降序返回
     *
     * @param userId 当前用户ID
     * @param modeId 彩票模式ID（必填）
     * @param query  查询条件（时间范围）
     * @return 号码频率列表（降序）
     */
    public List<NumberFrequencyVO> getNumberFrequency(Long userId, Long modeId,
                                                       StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);

        // 查询该用户该模式的所有历史开奖数据
        LambdaQueryWrapper<DrawResult> wrapper = new LambdaQueryWrapper<DrawResult>()
                .eq(DrawResult::getUserId, userId)
                .eq(DrawResult::getModeId, modeId)
                .eq(DrawResult::getDeleted, 0);

        if (startDate != null) {
            wrapper.ge(DrawResult::getDrawDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(DrawResult::getDrawDate, endDate);
        }

        List<DrawResult> results = drawResultMapper.selectList(wrapper);

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        // Java 层解析 red_numbers 字段，统计各号码出现频次
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (DrawResult dr : results) {
            if (dr.getRedNumbers() == null || dr.getRedNumbers().trim().isEmpty()) {
                continue;
            }
            String[] parts = dr.getRedNumbers().split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        int num = Integer.parseInt(trimmed);
                        freqMap.merge(num, 1, Integer::sum);
                    } catch (NumberFormatException ignored) {
                        // 忽略非法号码
                    }
                }
            }
        }

        // 按频次降序排列
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(e -> NumberFrequencyVO.builder()
                        .number(e.getKey())
                        .frequency(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取刮刮乐消费与中奖趋势数据
     */
    public List<TrendPointVO> getScratchTrend(Long userId, StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);
        String groupBy      = resolveGroupBy(query);

        List<Map<String, Object>> rows = scratchTicketMapper.getTrendStats(
                userId, groupBy, startDate, endDate);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream().map(row -> TrendPointVO.builder()
                .period(row.get("period") == null ? "" : row.get("period").toString())
                .cost(toBigDecimal(row.get("cost")))
                .win(toBigDecimal(row.get("win")))
                .build()
        ).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 5. 高级统计
    // -------------------------------------------------------------------------

    /**
     * 获取高级统计数据
     * 包含财务、期数、空窗/连中、中奖记录、成就徽章、高光时间轴等维度
     */
    public AdvancedStatisticsVO getAdvancedStatistics(Long userId, StatisticsQueryRequest query) {
        LocalDate startDate = resolveStartDate(query);
        LocalDate endDate   = resolveEndDate(query);

        // 查询范围内所有摇奖票（按开奖时间升序，便于计算连中/空窗）
        // 时间过滤：优先用 draw_time，没有则回退 bet_time
        LambdaQueryWrapper<DrawTicket> wrapper = new LambdaQueryWrapper<DrawTicket>()
                .eq(DrawTicket::getUserId, userId)
                .eq(query.getModeId() != null, DrawTicket::getModeId, query.getModeId());

        // 时间范围过滤：COALESCE(draw_time, bet_time)
        if (startDate != null) {
            wrapper.apply("DATE(COALESCE(draw_time, bet_time)) >= {0}", startDate.toString());
        }
        if (endDate != null) {
            wrapper.apply("DATE(COALESCE(draw_time, bet_time)) <= {0}", endDate.toString());
        }

        // 按开奖时间升序（无开奖时间则按投注时间）
        wrapper.last("ORDER BY COALESCE(draw_time, bet_time) ASC");

        List<DrawTicket> tickets = drawTicketMapper.selectList(wrapper);

        if (tickets.isEmpty()) {
            return AdvancedStatisticsVO.builder()
                    .totalCost(BigDecimal.ZERO).totalWin(BigDecimal.ZERO)
                    .netProfit(BigDecimal.ZERO).roi(0.0)
                    .totalIssues(0).totalTickets(0)
                    .avgCostPerIssue(BigDecimal.ZERO)
                    .currentBlankStreak(0).maxWinStreak(0).maxBlankStreak(0)
                    .winRate(0.0).winTickets(0).avgWinInterval(0.0).bigWinCount(0)
                    .winLevelDistribution(Collections.emptyMap())
                    .winLevelNames(Collections.emptyMap())
                    .achievements(Collections.emptyList())
                    .highlights(Collections.emptyList())
                    .build();
        }

        // ---- 基础财务 ----
        BigDecimal totalCost = tickets.stream()
                .map(t -> t.getBetAmount() != null ? t.getBetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWin = tickets.stream()
                .filter(t -> "WIN".equals(t.getWinStatus()))
                .map(t -> t.getWinAmount() != null ? t.getWinAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netProfit = totalWin.subtract(totalCost);
        double roi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : totalWin.subtract(totalCost)
                    .divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

        // ---- 期数/注数 ----
        long totalIssues = tickets.stream()
                .map(DrawTicket::getIssueNo)
                .filter(s -> s != null && !s.isEmpty())
                .distinct().count();
        long totalTickets = tickets.size();
        BigDecimal avgCostPerIssue = totalIssues == 0 ? BigDecimal.ZERO
                : totalCost.divide(BigDecimal.valueOf(totalIssues), 2, RoundingMode.HALF_UP);

        // ---- 中奖注数/率 ----
        List<DrawTicket> winTicketList = tickets.stream()
                .filter(t -> "WIN".equals(t.getWinStatus()))
                .collect(Collectors.toList());
        long winTickets = winTicketList.size();
        double winRate = totalTickets == 0 ? 0.0
                : BigDecimal.valueOf(winTickets)
                    .divide(BigDecimal.valueOf(totalTickets), 4, RoundingMode.HALF_UP)
                    .doubleValue();

        // ---- 最大投注/最高中奖 ----
        BigDecimal maxBetAmount = tickets.stream()
                .map(t -> t.getBetAmount() != null ? t.getBetAmount() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxSingleWin = winTicketList.stream()
                .map(t -> t.getWinAmount() != null ? t.getWinAmount() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        Optional<DrawTicket> bestWinTicket = winTicketList.stream()
                .filter(t -> t.getWinLevel() != null)
                .min(Comparator.comparingInt(DrawTicket::getWinLevel));
        Integer bestWinLevel = bestWinTicket.map(DrawTicket::getWinLevel).orElse(null);

        // ---- 首次/最近中奖 ----
        // 中奖时间优先使用 draw_time（开奖时间），没有则回退到 bet_time
        DrawTicket firstWin = winTicketList.isEmpty() ? null : winTicketList.get(0);
        DrawTicket lastWin  = winTicketList.isEmpty() ? null : winTicketList.get(winTicketList.size() - 1);

        // ---- 连中/空窗 ----
        long maxWinStreak = 0, maxBlankStreak = 0;
        long curWin = 0, curBlank = 0, currentBlankStreak = 0;
        for (DrawTicket t : tickets) {
            if ("WIN".equals(t.getWinStatus())) {
                curWin++;
                curBlank = 0;
                maxWinStreak = Math.max(maxWinStreak, curWin);
            } else if ("NO_WIN".equals(t.getWinStatus())) {
                curBlank++;
                curWin = 0;
                maxBlankStreak = Math.max(maxBlankStreak, curBlank);
            }
        }
        // 当前空窗：从最后往前数连续未中奖
        for (int i = tickets.size() - 1; i >= 0; i--) {
            String s = tickets.get(i).getWinStatus();
            if ("NO_WIN".equals(s)) currentBlankStreak++;
            else break;
        }

        // ---- 平均中奖间隔 ----
        double avgWinInterval = 0.0;
        if (winTickets >= 2) {
            // 找出每次中奖在 tickets 列表中的索引，计算相邻间隔
            List<Integer> winIndexes = new ArrayList<>();
            for (int i = 0; i < tickets.size(); i++) {
                if ("WIN".equals(tickets.get(i).getWinStatus())) winIndexes.add(i);
            }
            int totalGap = 0;
            for (int i = 1; i < winIndexes.size(); i++) {
                totalGap += winIndexes.get(i) - winIndexes.get(i - 1);
            }
            avgWinInterval = (double) totalGap / (winIndexes.size() - 1);
        }

        // ---- 大额中奖次数（>= 1000元）----
        long bigWinCount = winTicketList.stream()
                .filter(t -> t.getWinAmount() != null
                        && t.getWinAmount().compareTo(new BigDecimal("1000")) >= 0)
                .count();

        // ---- 各等奖分布 ----
        Map<Integer, Long> winLevelDistribution = winTicketList.stream()
                .filter(t -> t.getWinLevel() != null)
                .collect(Collectors.groupingBy(DrawTicket::getWinLevel, Collectors.counting()));

        // 等奖名称
        Map<Integer, String> winLevelNames = new HashMap<>();
        if (!winLevelDistribution.isEmpty() && query.getModeId() != null) {
            try {
                List<WinRule> rules = winRuleService.findRulesByModeId(query.getModeId());
                rules.forEach(r -> winLevelNames.put(r.getWinLevel(), r.getLevelName()));
            } catch (Exception ignored) {}
        }
        if (bestWinLevel != null && !winLevelNames.containsKey(bestWinLevel)) {
            winLevelNames.put(bestWinLevel, bestWinLevel + "等奖");
        }

        // 最高等级名称
        String bestWinLevelName = bestWinLevel != null
                ? winLevelNames.getOrDefault(bestWinLevel, bestWinLevel + "等奖") : null;

        // ---- 刮刮乐专属统计（仅在未指定 modeId 时合并）----
        BigDecimal scratchTotalCost = BigDecimal.ZERO;
        BigDecimal scratchTotalWin  = BigDecimal.ZERO;
        long scratchTotalQuantity   = 0L;
        long scratchWinCount        = 0L;
        BigDecimal scratchMaxSingleWin = BigDecimal.ZERO;
        long scratchBigWinCount     = 0L;
        LocalDateTime scratchFirstDate    = null;  // 最早刮奖日期
        LocalDateTime scratchFirstWinDate = null;  // 首次中奖刮奖日期
        LocalDateTime scratchLastWinDate  = null;  // 最近中奖刮奖日期
        LocalDateTime scratchMaxWinDate   = null;  // 最高单笔中奖刮奖日期

        if (query.getModeId() == null) {
            try {
                Map<String, Object> scratchStats = scratchTicketMapper.getAdvancedStats(
                        userId, startDate, endDate);
                scratchTotalCost     = toBigDecimal(scratchStats.get("totalCost"));
                scratchTotalWin      = toBigDecimal(scratchStats.get("totalWin"));
                scratchTotalQuantity = toLong(scratchStats.get("totalQuantity"));
                scratchWinCount      = toLong(scratchStats.get("winCount"));
                scratchMaxSingleWin  = toBigDecimal(scratchStats.get("maxSingleWin"));
                scratchBigWinCount   = toLong(scratchStats.get("bigWinCount"));
            } catch (Exception e) {
                log.warn("刮刮乐高级统计查询失败: {}", e.getMessage());
            }
            try {
                Map<String, Object> firstDates = scratchTicketMapper.getFirstDates(
                        userId, startDate, endDate);
                Object fd = firstDates.get("firstScratchDate");
                Object fw = firstDates.get("firstWinDate");
                Object lw = firstDates.get("lastWinDate");
                Object mw = firstDates.get("maxWinDate");
                if (fd != null) {
                    scratchFirstDate = toLocalDate(fd).atStartOfDay();
                }
                if (fw != null) {
                    scratchFirstWinDate = toLocalDate(fw).atStartOfDay();
                }
                if (lw != null) {
                    scratchLastWinDate = toLocalDate(lw).atStartOfDay();
                }
                if (mw != null) {
                    scratchMaxWinDate = toLocalDate(mw).atStartOfDay();
                }
            } catch (Exception e) {
                log.warn("刮刮乐首次日期查询失败: {}", e.getMessage());
            }
        }

        BigDecimal scratchNetProfit = scratchTotalWin.subtract(scratchTotalCost);
        double scratchWinRate = scratchTotalQuantity == 0 ? 0.0
                : BigDecimal.valueOf(scratchWinCount)
                    .divide(BigDecimal.valueOf(scratchTotalQuantity), 4, RoundingMode.HALF_UP)
                    .doubleValue();

        // ---- 成就徽章 ----
        // 最早购买时间：摇奖用 draw_time 优先（开奖时间），没有则用 bet_time
        LocalDateTime firstTicketTime = tickets.isEmpty() ? null
                : tickets.stream()
                    .map(t -> t.getDrawTime() != null ? t.getDrawTime() : t.getBetTime())
                    .filter(java.util.Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

        List<AdvancedStatisticsVO.AchievementBadge> achievements = buildAchievements(
                totalTickets, winTickets, maxSingleWin, maxWinStreak, bigWinCount,
                bestWinLevel, firstWin, firstTicketTime,
                scratchTotalQuantity, scratchWinCount, scratchMaxSingleWin, scratchBigWinCount,
                scratchFirstDate, scratchFirstWinDate);

        // ---- 高光时间轴 ----
        List<AdvancedStatisticsVO.HighlightEvent> highlights = buildHighlights(
                firstWin, lastWin, bestWinTicket.orElse(null), maxSingleWin,
                winTicketList, maxWinStreak,
                scratchTotalCost, scratchTotalWin, scratchMaxSingleWin,
                scratchFirstWinDate, scratchLastWinDate, scratchMaxWinDate);

        // 合并刮刮乐到总财务（totalCost/totalWin 已在 getOverview 里合并，这里 advanced 单独计算）
        BigDecimal combinedCost   = totalCost.add(scratchTotalCost);
        BigDecimal combinedWin    = totalWin.add(scratchTotalWin);
        BigDecimal combinedProfit = combinedWin.subtract(combinedCost);
        double combinedRoi = combinedCost.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : combinedWin.subtract(combinedCost)
                    .divide(combinedCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

        return AdvancedStatisticsVO.builder()
                // 合并后的总财务
                .totalCost(combinedCost).totalWin(combinedWin)
                .netProfit(combinedProfit).roi(combinedRoi)
                .totalIssues(totalIssues).totalTickets(totalTickets)
                .avgCostPerIssue(avgCostPerIssue)
                .currentBlankStreak(currentBlankStreak)
                .maxWinStreak(maxWinStreak).maxBlankStreak(maxBlankStreak)
                .firstWinTime(firstWin != null ? winTime(firstWin) : null)
                .firstWinIssueNo(firstWin != null ? firstWin.getIssueNo() : null)
                .lastWinTime(lastWin != null ? winTime(lastWin) : null)
                .lastWinIssueNo(lastWin != null ? lastWin.getIssueNo() : null)
                .maxSingleWin(maxSingleWin).bestWinLevel(bestWinLevel)
                .bestWinLevelName(bestWinLevelName).maxBetAmount(maxBetAmount)
                .winRate(winRate).winTickets(winTickets)
                .avgWinInterval(avgWinInterval).bigWinCount(bigWinCount)
                .winLevelDistribution(winLevelDistribution)
                .winLevelNames(winLevelNames)
                .achievements(achievements).highlights(highlights)
                // 刮刮乐专属
                .scratchTotalCost(scratchTotalCost)
                .scratchTotalWin(scratchTotalWin)
                .scratchNetProfit(scratchNetProfit)
                .scratchTotalQuantity(scratchTotalQuantity)
                .scratchWinCount(scratchWinCount)
                .scratchWinRate(scratchWinRate)
                .scratchMaxSingleWin(scratchMaxSingleWin)
                .scratchBigWinCount(scratchBigWinCount)
                .build();
    }

    private List<AdvancedStatisticsVO.AchievementBadge> buildAchievements(
            long totalTickets, long winTickets, BigDecimal maxSingleWin,
            long maxWinStreak, long bigWinCount, Integer bestWinLevel,
            DrawTicket firstWin, LocalDateTime firstTicketTime,
            long scratchTotalQuantity, long scratchWinCount,
            BigDecimal scratchMaxSingleWin, long scratchBigWinCount,
            LocalDateTime scratchFirstDate, LocalDateTime scratchFirstWinDate) {

        List<AdvancedStatisticsVO.AchievementBadge> list = new ArrayList<>();
        long totalAll = totalTickets + scratchTotalQuantity;
        long winAll   = winTickets + scratchWinCount;
        BigDecimal maxWinAll = maxSingleWin.max(scratchMaxSingleWin);
        long bigWinAll = bigWinCount + scratchBigWinCount;

        // 最早购买时间：摇奖开奖时间 vs 刮刮乐刮奖日期，取最早
        LocalDateTime drawFirstTime = firstTicketTime; // 已是 draw_time 优先
        LocalDateTime earliestPurchase = earlier(drawFirstTime, scratchFirstDate);

        // 首次中奖时间：摇奖开奖时间 vs 刮刮乐刮奖日期，取最早
        LocalDateTime drawFirstWinTime = firstWin != null ? winTime(firstWin) : null;
        LocalDateTime earliestWin = earlier(drawFirstWinTime, scratchFirstWinDate);

        // ---- 入门成就 ----
        list.add(badge("first_ticket", "初出茅庐", "🎫",
                "购买第一注彩票或第一张刮刮乐",
                totalAll >= 1, earliestPurchase));

        list.add(badge("first_win", "初尝甜头", "🎉",
                "首次中奖（摇奖或刮刮乐）",
                winAll >= 1, earliestWin));

        // ---- 数量成就 ----
        list.add(badge("tickets_50", "小试牛刀", "🌱",
                "摇奖注数 + 刮刮乐张数合计达到50",
                totalAll >= 50, null));

        list.add(badge("hundred_tickets", "百注老手", "💯",
                "摇奖注数 + 刮刮乐张数合计达到100",
                totalAll >= 100, null));

        list.add(badge("tickets_500", "坚持就是胜利", "💪",
                "摇奖注数 + 刮刮乐张数合计达到500",
                totalAll >= 500, null));

        list.add(badge("tickets_1000", "彩票达人", "🎖️",
                "摇奖注数 + 刮刮乐张数合计达到1000",
                totalAll >= 1000, null));

        // ---- 中奖成就 ----
        list.add(badge("win_5", "小有斩获", "🥉",
                "累计中奖5次（摇奖+刮刮乐）",
                winAll >= 5, null));

        list.add(badge("ten_wins", "常胜将军", "🏆",
                "累计中奖10次（摇奖+刮刮乐）",
                winAll >= 10, null));

        list.add(badge("win_50", "中奖专业户", "🥇",
                "累计中奖50次（摇奖+刮刮乐）",
                winAll >= 50, null));

        // ---- 金额成就 ----
        list.add(badge("big_win", "大奖得主", "💰",
                "单笔中奖超过1000元",
                maxWinAll.compareTo(new BigDecimal("1000")) >= 0, null));

        list.add(badge("super_win", "超级大奖", "🌟",
                "单笔中奖超过10000元",
                maxWinAll.compareTo(new BigDecimal("10000")) >= 0, null));

        list.add(badge("big_win_5", "大奖收割机", "⚡",
                "累计5次大额中奖（单笔≥1000元）",
                bigWinAll >= 5, null));

        // ---- 摇奖专属 ----
        list.add(badge("win_streak_3", "三连中", "🔥",
                "摇奖连续3注中奖",
                maxWinStreak >= 3, null));

        list.add(badge("win_streak_5", "五连中", "🌈",
                "摇奖连续5注中奖",
                maxWinStreak >= 5, null));

        list.add(badge("top_prize", "头奖得主", "👑",
                "摇奖获得一等奖",
                bestWinLevel != null && bestWinLevel == 1, null));

        list.add(badge("second_prize", "二等奖得主", "🥈",
                "摇奖获得二等奖",
                bestWinLevel != null && bestWinLevel <= 2, null));

        // ---- 刮刮乐专属 ----
        list.add(badge("scratch_100", "刮刮乐爱好者", "🎰",
                "刮刮乐累计购买100张",
                scratchTotalQuantity >= 100, null));

        list.add(badge("scratch_win_10", "刮刮乐常胜", "🎊",
                "刮刮乐累计中奖10次",
                scratchWinCount >= 10, null));

        return list;
    }

    /** 快捷构建成就徽章 */
    private AdvancedStatisticsVO.AchievementBadge badge(
            String id, String name, String icon, String desc,
            boolean unlocked, java.time.LocalDateTime unlockedAt) {
        return AdvancedStatisticsVO.AchievementBadge.builder()
                .id(id).name(name).icon(icon).description(desc)
                .unlocked(unlocked).unlockedAt(unlockedAt)
                .build();
    }

    private List<AdvancedStatisticsVO.HighlightEvent> buildHighlights(
            DrawTicket firstWin, DrawTicket lastWin, DrawTicket bestWinTicket,
            BigDecimal maxSingleWin, List<DrawTicket> winTicketList, long maxWinStreak,
            BigDecimal scratchTotalCost, BigDecimal scratchTotalWin,
            BigDecimal scratchMaxSingleWin, LocalDateTime scratchFirstWinDate,
            LocalDateTime scratchLastWinDate, LocalDateTime scratchMaxWinDate) {

        List<AdvancedStatisticsVO.HighlightEvent> list = new ArrayList<>();

        // 首次中奖
        if (firstWin != null) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("FIRST_WIN").title("首次中奖 🎉")
                    .description("开启了中奖之旅")
                    .time(winTime(firstWin)).issueNo(firstWin.getIssueNo())
                    .amount(firstWin.getWinAmount())
                    .build());
        }

        // 最高等级（仅一二三等奖值得高光）
        if (bestWinTicket != null && bestWinTicket.getWinLevel() != null
                && bestWinTicket.getWinLevel() <= 3) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("BEST_LEVEL").title("最高等级中奖 👑")
                    .description("摇奖 " + bestWinTicket.getWinLevel() + " 等奖")
                    .time(winTime(bestWinTicket)).issueNo(bestWinTicket.getIssueNo())
                    .amount(bestWinTicket.getWinAmount())
                    .build());
        }

        // 摇奖最高单笔
        if (maxSingleWin.compareTo(BigDecimal.ZERO) > 0) {
            winTicketList.stream()
                    .filter(t -> maxSingleWin.equals(t.getWinAmount()))
                    .findFirst()
                    .ifPresent(t -> list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                            .type("MAX_WIN").title("摇奖最高单笔 💰")
                            .description("¥" + maxSingleWin.toPlainString())
                            .time(winTime(t)).issueNo(t.getIssueNo())
                            .amount(maxSingleWin)
                            .build()));
        }

        // 刮刮乐最高单笔（用最高单笔中奖那条记录的刮奖日期）
        if (scratchMaxSingleWin.compareTo(BigDecimal.ZERO) > 0) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("SCRATCH_MAX_WIN").title("刮刮乐最高单笔 🎰")
                    .description("¥" + scratchMaxSingleWin.toPlainString())
                    .time(scratchMaxWinDate)
                    .amount(scratchMaxSingleWin)
                    .build());
        }

        // 连中记录
        if (maxWinStreak >= 3) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("WIN_STREAK").title("最长连中 🔥")
                    .description("摇奖连续 " + maxWinStreak + " 注中奖")
                    .build());
        }

        // 最近中奖：摇奖 vs 刮刮乐，取最新的
        LocalDateTime drawLastWinTime = lastWin != null ? winTime(lastWin) : null;
        boolean scratchIsLater = drawLastWinTime == null
                || (scratchLastWinDate != null && scratchLastWinDate.isAfter(drawLastWinTime));

        if (scratchIsLater && scratchLastWinDate != null) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("LAST_WIN").title("最近中奖 ⭐")
                    .description("刮刮乐最近一次中奖")
                    .time(scratchLastWinDate)
                    .build());
        } else if (lastWin != null && lastWin != firstWin) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("LAST_WIN").title("最近中奖 ⭐")
                    .description("摇奖最近一次中奖")
                    .time(winTime(lastWin)).issueNo(lastWin.getIssueNo())
                    .amount(lastWin.getWinAmount())
                    .build());
        }

        // 刮刮乐总盈亏里程碑
        BigDecimal scratchProfit = scratchTotalWin.subtract(scratchTotalCost);
        if (scratchProfit.compareTo(BigDecimal.ZERO) > 0) {
            list.add(AdvancedStatisticsVO.HighlightEvent.builder()
                    .type("SCRATCH_PROFIT").title("刮刮乐盈利 📈")
                    .description("刮刮乐累计盈利 ¥" + scratchProfit.toPlainString())
                    .time(scratchLastWinDate)
                    .amount(scratchProfit)
                    .build());
        }

        // 按时间排序（无时间的排最后）
        list.sort(Comparator.comparing(
                e -> e.getTime() != null ? e.getTime() : LocalDateTime.MAX));

        return list;
    }

    /**
     * 获取摇奖票的中奖时间：优先使用 draw_time（开奖时间），没有则回退到 bet_time
     */
    private LocalDateTime winTime(DrawTicket ticket) {
        if (ticket == null) return null;
        return ticket.getDrawTime() != null ? ticket.getDrawTime() : ticket.getBetTime();
    }

    /** 取两个时间中较早的，null 视为无穷大 */
    private LocalDateTime earlier(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    /** 将 SQL 返回的日期对象转为 LocalDate */
    private java.time.LocalDate toLocalDate(Object val) {
        if (val instanceof java.time.LocalDate) return (java.time.LocalDate) val;
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        if (val instanceof java.util.Date) return ((java.util.Date) val).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return java.time.LocalDate.parse(val.toString().substring(0, 10));
    }
}
