package com.lottery.controller;

import com.lottery.common.PageResult;
import com.lottery.common.Result;
import com.lottery.dto.drawticket.DrawTicketCreateRequest;
import com.lottery.dto.drawticket.DrawTicketQueryRequest;
import com.lottery.dto.drawticket.DrawTicketUpdateRequest;
import com.lottery.dto.drawticket.WinStatusUpdateRequest;
import com.lottery.service.DrawTicketService;
import com.lottery.util.SecurityUtil;
import com.lottery.vo.DrawTicketVO;
import com.lottery.vo.TopWinVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 摇奖票控制器
 * 提供摇奖票的增删改查及中奖状态管理 RESTful 接口
 */
@RestController
@RequestMapping("/api/draw-tickets")
@RequiredArgsConstructor
public class DrawTicketController {

    private final DrawTicketService drawTicketService;

    /**
     * 分页查询当前用户的摇奖票列表
     *
     * @param query 查询条件（modeId、issueNo、winStatus、pageNum、pageSize）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<DrawTicketVO>> pageQuery(@ModelAttribute DrawTicketQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.pageQuery(userId, query));
    }

    /**
     * 查询历史最佳中奖记录 Top N
     * GET /api/draw-tickets/top-wins?sortBy=level&topN=5&modeId=1
     *
     * @param sortBy 排序方式：level（按等级，默认）或 amount（按金额）
     * @param topN   返回条数（默认5，最大20）
     * @param modeId 彩票模式过滤（可选）
     * @return Top N 中奖记录
     */
    @GetMapping("/top-wins")
    public Result<List<TopWinVO>> getTopWins(
            @RequestParam(defaultValue = "level") String sortBy,
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(required = false) Long modeId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.getTopWins(userId, sortBy, topN, modeId));
    }

    /**
     * 根据 ID 获取单条摇奖票
     *
     * @param id 摇奖票 ID
     * @return 摇奖票 VO
     */
    @GetMapping("/{id}")
    public Result<DrawTicketVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.getById(id, userId));
    }

    /**
     * 创建单条摇奖票
     *
     * @param request 创建请求体
     * @return 创建后的摇奖票 VO
     */
    @PostMapping
    public Result<DrawTicketVO> create(@Valid @RequestBody DrawTicketCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.create(request, userId));
    }

    /**
     * 批量创建摇奖票
     *
     * @param requests 创建请求体列表
     * @return 创建后的摇奖票 VO 列表
     */
    @PostMapping("/batch")
    public Result<List<DrawTicketVO>> createBatch(@Valid @RequestBody List<DrawTicketCreateRequest> requests) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.createBatch(requests, userId));
    }

    /**
     * 更新摇奖票（仅更新非空字段）
     *
     * @param id      摇奖票 ID
     * @param request 更新请求体
     * @return 更新后的摇奖票 VO
     */
    @PutMapping("/{id}")
    public Result<DrawTicketVO> update(@PathVariable Long id,
                                       @RequestBody DrawTicketUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.update(id, request, userId));
    }

    /**
     * 软删除摇奖票
     *
     * @param id 摇奖票 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> softDelete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        drawTicketService.softDelete(id, userId);
        return Result.success();
    }

    /**
     * 手动设置摇奖票中奖状态
     *
     * @param id      摇奖票 ID
     * @param request 中奖状态更新请求体
     * @return 更新后的摇奖票 VO
     */
    @PutMapping("/{id}/win-status")
    public Result<DrawTicketVO> updateWinStatus(@PathVariable Long id,
                                                @Valid @RequestBody WinStatusUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.updateWinStatus(id, request, userId));
    }

    /**
     * 更新摇奖票兑奖状态
     *
     * @param id        摇奖票 ID
     * @param isClaimed 是否已兑奖：0未兑奖 1已兑奖
     * @return 更新后的摇奖票 VO
     */
    @PutMapping("/{id}/claim-status")
    public Result<DrawTicketVO> updateClaimStatus(@PathVariable Long id,
                                                   @RequestParam Integer isClaimed) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.updateClaimStatus(id, isClaimed, userId));
    }

    /**
     * 检查是否存在重复的摇奖票（提交前调用）
     *
     * @param request 创建请求体（与新增接口相同结构）
     * @return 重复的摇奖票列表，空列表表示无重复
     */
    @PostMapping("/check-duplicate")
    public Result<List<DrawTicketVO>> checkDuplicate(@RequestBody DrawTicketCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.checkDuplicate(request, userId));
    }

    /**
     * 批量补填历史摇奖票的中奖号码
     * 针对有期号但 winning_numbers 为空的记录，根据官方开奖结果重新计算
     *
     * @return 处理结果：total/updated/skipped
     */
    @PostMapping("/backfill-winning-numbers")
    public Result<java.util.Map<String, Integer>> backfillWinningNumbers() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawTicketService.backfillWinningNumbers(userId));
    }
}
