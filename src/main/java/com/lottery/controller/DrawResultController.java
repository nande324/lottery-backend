package com.lottery.controller;

import com.lottery.common.PageResult;
import com.lottery.common.Result;
import com.lottery.dto.drawresult.DrawResultCreateRequest;
import com.lottery.dto.drawresult.DrawResultQueryRequest;
import com.lottery.dto.drawresult.DrawResultUpdateRequest;
import com.lottery.service.DrawResultService;
import com.lottery.util.SecurityUtil;
import com.lottery.vo.DrawResultVO;
import com.lottery.vo.WinCheckSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 开奖结果控制器
 * 提供开奖结果的增删改查及中奖核对 RESTful 接口
 */
@RestController
@RequestMapping("/api/draw-results")
@RequiredArgsConstructor
public class DrawResultController {

    private final DrawResultService drawResultService;

    /**
     * 分页查询当前用户的开奖结果列表
     *
     * @param query 查询条件（modeId、issueNo、pageNum、pageSize）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<DrawResultVO>> pageQuery(@ModelAttribute DrawResultQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawResultService.pageQuery(userId, query));
    }

    /**
     * 根据 ID 获取单条开奖结果
     *
     * @param id 开奖结果 ID
     * @return 开奖结果 VO
     */
    @GetMapping("/{id}")
    public Result<DrawResultVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawResultService.getById(id, userId));
    }

    /**
     * 手动录入开奖结果
     *
     * @param request 创建请求体
     * @return 创建后的开奖结果 VO
     */
    @PostMapping
    public Result<DrawResultVO> create(@Valid @RequestBody DrawResultCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawResultService.create(request, userId));
    }

    /**
     * 更新开奖结果（仅更新非空字段）
     *
     * @param id      开奖结果 ID
     * @param request 更新请求体
     * @return 更新后的开奖结果 VO
     */
    @PutMapping("/{id}")
    public Result<DrawResultVO> update(@PathVariable Long id,
                                       @RequestBody DrawResultUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawResultService.update(id, request, userId));
    }

    /**
     * 触发中奖核对
     * 对该期当前用户的所有摇奖票执行 WinCheck，更新中奖状态并返回汇总结果
     *
     * @param id 开奖结果 ID
     * @return 中奖核对汇总结果
     */
    @PostMapping("/{id}/win-check")
    public Result<WinCheckSummaryVO> winCheck(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(drawResultService.winCheck(id, userId));
    }

    /**
     * 同步外部开奖数据（占位接口，暂未实现）
     *
     * @return 501 未实现错误
     */
    @PostMapping("/sync")
    public Result<Void> sync() {
        return Result.error(501, "外部API同步功能暂未实现");
    }
}
