package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.lotterymode.LotteryModeCreateRequest;
import com.lottery.dto.lotterymode.LotteryModeUpdateRequest;
import com.lottery.service.LotteryModeService;
import com.lottery.vo.LotteryModeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 彩票模式控制器
 * 提供彩票模式的增删改查 RESTful 接口
 */
@RestController
@RequestMapping("/api/lottery-modes")
@RequiredArgsConstructor
public class LotteryModeController {

    private final LotteryModeService lotteryModeService;

    /**
     * 获取所有彩票模式列表
     *
     * @return 彩票模式 VO 列表
     */
    @GetMapping
    public Result<List<LotteryModeVO>> listAll() {
        return Result.success(lotteryModeService.listAll());
    }

    /**
     * 根据 ID 获取单个彩票模式
     *
     * @param id 彩票模式 ID
     * @return 彩票模式 VO
     */
    @GetMapping("/{id}")
    public Result<LotteryModeVO> getById(@PathVariable Long id) {
        return Result.success(lotteryModeService.getById(id));
    }

    /**
     * 新增彩票模式（仅管理员）
     *
     * @param request 创建请求体
     * @return 新增后的彩票模式 VO
     */
    @PostMapping
    public Result<LotteryModeVO> create(@Valid @RequestBody LotteryModeCreateRequest request) {
        return Result.success(lotteryModeService.create(request));
    }

    /**
     * 更新彩票模式（仅管理员）
     *
     * @param id      彩票模式 ID
     * @param request 更新请求体
     * @return 更新后的彩票模式 VO
     */
    @PutMapping("/{id}")
    public Result<LotteryModeVO> update(@PathVariable Long id,
                                        @RequestBody LotteryModeUpdateRequest request) {
        return Result.success(lotteryModeService.update(id, request));
    }

    /**
     * 删除彩票模式（仅管理员，软删除）
     *
     * @param id 彩票模式 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        lotteryModeService.deleteById(id);
        return Result.success();
    }
}
