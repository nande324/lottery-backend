package com.lottery.controller;

import com.lottery.common.PageResult;
import com.lottery.common.Result;
import com.lottery.dto.scratchticket.ScratchTicketCreateRequest;
import com.lottery.dto.scratchticket.ScratchTicketQueryRequest;
import com.lottery.dto.scratchticket.ScratchTicketUpdateRequest;
import com.lottery.service.ScratchTicketService;
import com.lottery.util.SecurityUtil;
import com.lottery.vo.ScratchTicketVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 刮刮乐记录控制器
 * 提供刮刮乐记录的增删改查 RESTful 接口
 */
@RestController
@RequestMapping("/api/scratch-tickets")
@RequiredArgsConstructor
public class ScratchTicketController {

    private final ScratchTicketService scratchTicketService;

    /**
     * 分页查询当前用户的刮刮乐记录列表
     *
     * @param query 查询条件（startDate、endDate、scratchType、pageNum、pageSize）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<ScratchTicketVO>> pageQuery(@ModelAttribute ScratchTicketQueryRequest query) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scratchTicketService.pageQuery(userId, query));
    }

    /**
     * 创建刮刮乐记录
     *
     * @param request 创建请求体
     * @return 创建后的刮刮乐记录 VO
     */
    @PostMapping
    public Result<ScratchTicketVO> create(@Valid @RequestBody ScratchTicketCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scratchTicketService.create(request, userId));
    }

    /**
     * 更新刮刮乐记录（仅更新非空字段）
     *
     * @param id      刮刮乐记录 ID
     * @param request 更新请求体
     * @return 更新后的刮刮乐记录 VO
     */
    @PutMapping("/{id}")
    public Result<ScratchTicketVO> update(@PathVariable Long id,
                                          @RequestBody ScratchTicketUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scratchTicketService.update(id, request, userId));
    }

    /**
     * 软删除刮刮乐记录
     *
     * @param id 刮刮乐记录 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> softDelete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        scratchTicketService.softDelete(id, userId);
        return Result.success();
    }
}
