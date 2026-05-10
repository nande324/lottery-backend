package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.defaultnumber.DefaultNumberCreateRequest;
import com.lottery.dto.defaultnumber.DefaultNumberUpdateRequest;
import com.lottery.service.DefaultNumberService;
import com.lottery.vo.DefaultNumberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 默认号码控制器
 * 提供默认号码的增删改查 RESTful 接口
 */
@RestController
@RequestMapping("/api/default-numbers")
@RequiredArgsConstructor
public class DefaultNumberController {

    private final DefaultNumberService defaultNumberService;

    /**
     * 获取所有默认号码列表
     *
     * @return 默认号码VO列表
     */
    @GetMapping
    public Result<List<DefaultNumberVO>> listAll() {
        return Result.success(defaultNumberService.listAll());
    }

    /**
     * 根据彩票模式ID获取默认号码列表
     *
     * @param modeId 彩票模式ID
     * @return 默认号码VO列表
     */
    @GetMapping("/mode/{modeId}")
    public Result<List<DefaultNumberVO>> listByModeId(@PathVariable Long modeId) {
        return Result.success(defaultNumberService.listByModeId(modeId));
    }

    /**
     * 根据ID获取单个默认号码
     *
     * @param id 默认号码ID
     * @return 默认号码VO
     */
    @GetMapping("/{id}")
    public Result<DefaultNumberVO> getById(@PathVariable Long id) {
        return Result.success(defaultNumberService.getById(id));
    }

    /**
     * 新增默认号码
     *
     * @param request 创建请求体
     * @return 新增后的默认号码VO
     */
    @PostMapping
    public Result<DefaultNumberVO> create(@Valid @RequestBody DefaultNumberCreateRequest request) {
        return Result.success(defaultNumberService.create(request));
    }

    /**
     * 更新默认号码
     *
     * @param id      默认号码ID
     * @param request 更新请求体
     * @return 更新后的默认号码VO
     */
    @PutMapping("/{id}")
    public Result<DefaultNumberVO> update(@PathVariable Long id,
                                          @RequestBody DefaultNumberUpdateRequest request) {
        return Result.success(defaultNumberService.update(id, request));
    }

    /**
     * 删除默认号码（软删除）
     *
     * @param id 默认号码ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        defaultNumberService.deleteById(id);
        return Result.success();
    }
}
