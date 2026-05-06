package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.number.GenerateRequest;
import com.lottery.dto.number.ValidateRequest;
import com.lottery.entity.LotteryMode;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.util.NumberGenerator;
import com.lottery.util.NumberRule;
import com.lottery.util.NumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 号码生成与校验控制器
 * 提供随机号码生成、批量生成及号码合法性校验接口
 */
@RestController
@RequestMapping("/api/numbers")
@RequiredArgsConstructor
public class NumberController {

    private final NumberGenerator numberGenerator;
    private final NumberValidator numberValidator;
    private final LotteryModeMapper lotteryModeMapper;

    /**
     * 随机生成单注号码
     * POST /api/numbers/generate
     *
     * @param request 生成请求（包含 modeId）
     * @return 包含红球和蓝球号码的 Map
     */
    @PostMapping("/generate")
    public Result<Map<String, List<Integer>>> generate(@Valid @RequestBody GenerateRequest request) {
        LotteryMode mode = getLotteryModeOrThrow(request.getModeId());
        NumberRule rule = NumberRule.fromLotteryMode(mode);
        Map<String, List<Integer>> numbers = numberGenerator.generate(rule);
        return Result.success(numbers);
    }

    /**
     * 批量生成 N 注号码，确保每注互不完全相同
     * POST /api/numbers/generate-batch
     *
     * @param request 生成请求（包含 modeId 和 count）
     * @return 去重后的号码列表
     */
    @PostMapping("/generate-batch")
    public Result<List<Map<String, List<Integer>>>> generateBatch(@Valid @RequestBody GenerateRequest request) {
        LotteryMode mode = getLotteryModeOrThrow(request.getModeId());
        NumberRule rule = NumberRule.fromLotteryMode(mode);
        List<Map<String, List<Integer>>> numbers = numberGenerator.generateBatch(rule, request.getCount());
        return Result.success(numbers);
    }

    /**
     * 校验号码是否符合指定彩票模式的规则
     * POST /api/numbers/validate
     *
     * @param request 校验请求（包含 modeId、redNumbers、blueNumbers）
     * @return 校验结果
     */
    @PostMapping("/validate")
    public Result<NumberValidator.ValidationResult> validate(@Valid @RequestBody ValidateRequest request) {
        LotteryMode mode = getLotteryModeOrThrow(request.getModeId());
        NumberRule rule = NumberRule.fromLotteryMode(mode);
        NumberValidator.ValidationResult result = numberValidator.validate(
                request.getRedNumbers(),
                request.getBlueNumbers(),
                rule
        );
        return Result.success(result);
    }

    /**
     * 根据 ID 查询彩票模式，不存在则抛出 404 异常
     *
     * @param modeId 彩票模式 ID
     * @return 彩票模式实体
     */
    private LotteryMode getLotteryModeOrThrow(Long modeId) {
        LotteryMode mode = lotteryModeMapper.selectById(modeId);
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式不存在，ID: " + modeId);
        }
        return mode;
    }
}
