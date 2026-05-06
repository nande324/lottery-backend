package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.statistics.StatisticsQueryRequest;
import com.lottery.service.StatisticsService;
import com.lottery.util.SecurityUtil;
import com.lottery.vo.AdvancedStatisticsVO;
import com.lottery.vo.ModeDistributionVO;
import com.lottery.vo.NumberFrequencyVO;
import com.lottery.vo.StatisticsOverviewVO;
import com.lottery.vo.TrendPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计分析控制器
 * 提供综合概览、趋势、模式占比、号码频率等统计接口
 * 所有接口仅返回当前已登录用户的数据
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取综合统计概览
     * 包含总消费、总中奖、净盈亏、总注数、中奖注数、中奖率
     *
     * @param query 查询条件（modeId、timeRange、startDate、endDate、winStatus）
     * @return StatisticsOverviewVO
     */
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> getOverview(@ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getOverview(userId, query));
    }

    /**
     * 获取消费与中奖趋势数据
     * 根据 timeRange 按日/月/年分组返回趋势折线图数据
     *
     * @param query 查询条件
     * @return 趋势数据点列表（按时间升序）
     */
    @GetMapping("/trend")
    public Result<List<TrendPointVO>> getTrend(@ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getTrend(userId, query));
    }

    /**
     * 获取各彩票模式消费占比
     * 用于饼图展示各模式消费分布
     *
     * @param query 查询条件（时间范围）
     * @return 模式占比列表
     */
    @GetMapping("/mode-distribution")
    public Result<List<ModeDistributionVO>> getModeDistribution(
            @ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getModeDistribution(userId, query));
    }

    /**
     * 获取号码频率统计
     */
    @GetMapping("/number-frequency")
    public Result<List<NumberFrequencyVO>> getNumberFrequency(
            @RequestParam Long modeId,
            @ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getNumberFrequency(userId, modeId, query));
    }

    /**
     * 获取刮刮乐消费与中奖趋势数据
     */
    @GetMapping("/scratch-trend")
    public Result<List<TrendPointVO>> getScratchTrend(@ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getScratchTrend(userId, query));
    }

    /**
     * 获取高级统计数据
     * 包含财务、期数、空窗/连中、中奖记录、成就徽章、高光时间轴等维度
     */
    @GetMapping("/advanced")
    public Result<AdvancedStatisticsVO> getAdvanced(@ModelAttribute StatisticsQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(statisticsService.getAdvancedStatistics(userId, query));
    }
}
