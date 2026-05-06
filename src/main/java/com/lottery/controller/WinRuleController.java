package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.entity.WinRule;
import com.lottery.service.WinRuleService;
import com.lottery.vo.WinRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 中奖规则控制器
 * 提供中奖规则的查询及管理接口
 */
@RestController
@RequestMapping("/api/win-rules")
@RequiredArgsConstructor
public class WinRuleController {

    private final WinRuleService winRuleService;

    /**
     * 获取所有中奖规则，可按 modeId 过滤
     * GET /api/win-rules?modeId=1
     */
    @GetMapping
    public Result<List<WinRuleVO>> listAll(@RequestParam(required = false) Long modeId) {
        return Result.success(winRuleService.listAll(modeId));
    }

    /**
     * 获取指定彩票模式的中奖规则列表
     * GET /api/win-rules/{modeId}
     */
    @GetMapping("/{modeId}")
    public Result<List<WinRuleVO>> listByModeId(@PathVariable Long modeId) {
        return Result.success(winRuleService.listByModeId(modeId));
    }

    /**
     * 新增中奖规则（仅 ADMIN）
     * POST /api/win-rules
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<WinRuleVO> create(@RequestBody WinRule rule) {
        return Result.success(winRuleService.create(rule));
    }

    /**
     * 更新中奖规则（仅 ADMIN）
     * PUT /api/win-rules/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<WinRuleVO> update(@PathVariable Long id, @RequestBody WinRule rule) {
        return Result.success(winRuleService.update(id, rule));
    }

    /**
     * 删除中奖规则（仅 ADMIN）
     * DELETE /api/win-rules/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        winRuleService.deleteById(id);
        return Result.success(null);
    }
}
