package com.lottery.controller;

import com.lottery.common.PageResult;
import com.lottery.common.Result;
import com.lottery.service.OfficialDrawSyncService;
import com.lottery.vo.OfficialDrawResultVO;
import com.lottery.vo.SyncResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 官方历史开奖数据控制器
 * 提供历史开奖数据同步与查询接口
 */
@RestController
@RequestMapping("/api/official-draw")
@RequiredArgsConstructor
public class OfficialDrawController {

    private final OfficialDrawSyncService officialDrawSyncService;

    /**
     * 同步2022年至今的双色球历史开奖数据
     * 自动分页拉取所有数据，已存在的记录自动跳过
     *
     * @return 同步结果（新增数、跳过数、失败数）
     */
    @PostMapping("/sync")
    public Result<SyncResultVO> syncHistory() {
        SyncResultVO result = officialDrawSyncService.syncHistoryData();
        return Result.success(result);
    }

    /**
     * 同步指定期号范围的历史开奖数据
     *
     * @param startIssue 起始期号（如 2022001）
     * @param endIssue   结束期号（如 2026044）
     * @return 同步结果
     */
    @PostMapping("/sync/range")
    public Result<SyncResultVO> syncRange(
            @RequestParam String startIssue,
            @RequestParam String endIssue) {
        SyncResultVO result = officialDrawSyncService.syncHistoryData(startIssue, endIssue);
        return Result.success(result);
    }

    /**
     * 分页查询官方历史开奖数据
     *
     * @param issueNo  期号（可选，精确匹配）
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认20）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<OfficialDrawResultVO>> pageQuery(
            @RequestParam(required = false) String issueNo,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(officialDrawSyncService.pageQuery(issueNo, pageNum, pageSize));
    }
}
