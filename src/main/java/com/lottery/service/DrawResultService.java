package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lottery.common.PageResult;
import com.lottery.dto.drawresult.DrawResultCreateRequest;
import com.lottery.dto.drawresult.DrawResultQueryRequest;
import com.lottery.dto.drawresult.DrawResultUpdateRequest;
import com.lottery.entity.DrawResult;
import com.lottery.entity.DrawTicket;
import com.lottery.entity.LotteryMode;
import com.lottery.entity.WinRule;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.DrawResultMapper;
import com.lottery.mapper.DrawTicketMapper;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.service.wincheck.WinChecker;
import com.lottery.service.wincheck.WinCheckerFactory;
import com.lottery.service.wincheck.WinResult;
import com.lottery.util.NumberRule;
import com.lottery.util.NumberValidator;
import com.lottery.vo.DrawResultVO;
import com.lottery.vo.WinCheckSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开奖结果服务
 * 提供开奖结果的增删改查及中奖核对业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawResultService {

    private final DrawResultMapper drawResultMapper;
    private final DrawTicketMapper drawTicketMapper;
    private final LotteryModeMapper lotteryModeMapper;
    private final NumberValidator numberValidator;
    private final WinCheckerFactory winCheckerFactory;
    private final WinRuleService winRuleService;

    /**
     * 分页查询当前用户的开奖结果列表，按开奖日期降序
     *
     * @param userId 当前用户 ID
     * @param query  查询条件（modeId、issueNo 均为可选过滤）
     * @return 分页结果
     */
    public PageResult<DrawResultVO> pageQuery(Long userId, DrawResultQueryRequest query) {
        Page<DrawResult> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<DrawResult> wrapper = new LambdaQueryWrapper<DrawResult>()
                .eq(DrawResult::getUserId, userId)
                .eq(query.getModeId() != null, DrawResult::getModeId, query.getModeId())
                .eq(query.getIssueNo() != null && !query.getIssueNo().isEmpty(),
                        DrawResult::getIssueNo, query.getIssueNo())
                .orderByDesc(DrawResult::getDrawDate);

        Page<DrawResult> resultPage = drawResultMapper.selectPage(page, wrapper);

        List<DrawResultVO> voList = resultPage.getRecords().stream()
                .map(DrawResultVO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), voList);
    }

    /**
     * 根据 ID 查询单条开奖结果，并校验归属用户
     *
     * @param id     开奖结果 ID
     * @param userId 当前用户 ID
     * @return 开奖结果 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawResultVO getById(Long id, Long userId) {
        DrawResult result = drawResultMapper.selectById(id);
        validateOwnership(result, id, userId);
        return DrawResultVO.fromEntity(result);
    }

    /**
     * 手动录入开奖结果
     *
     * @param request 创建请求
     * @param userId  当前用户 ID
     * @return 创建后的开奖结果 VO
     * @throws ResourceNotFoundException 若彩票模式不存在则抛出
     * @throws IllegalArgumentException  若号码不符合规则则抛出
     * @throws DuplicateKeyException     若 (userId, modeId, issueNo) 已存在则抛出
     */
    public DrawResultVO create(DrawResultCreateRequest request, Long userId) {
        // 查询彩票模式，构建号码规则
        LotteryMode mode = lotteryModeMapper.selectById(request.getModeId());
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", request.getModeId());
        }

        NumberRule rule = NumberRule.fromLotteryMode(mode);

        // 校验号码合法性
        NumberValidator.ValidationResult validationResult = numberValidator.validate(
                request.getRedNumbers(),
                request.getBlueNumbers(),
                rule
        );
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getErrorMessage());
        }

        // 将号码列表转为逗号分隔字符串
        String redNumbersStr = joinNumbers(request.getRedNumbers());
        String blueNumbersStr = request.getBlueNumbers() != null && !request.getBlueNumbers().isEmpty()
                ? joinNumbers(request.getBlueNumbers())
                : null;

        DrawResult drawResult = DrawResult.builder()
                .userId(userId)
                .modeId(request.getModeId())
                .issueNo(request.getIssueNo())
                .drawDate(request.getDrawDate())
                .redNumbers(redNumbersStr)
                .blueNumbers(blueNumbersStr)
                .prizePool(request.getPrizePool())
                .source("MANUAL")
                .build();

        try {
            drawResultMapper.insert(drawResult);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException(
                    String.format("该期开奖结果已存在：模式ID=%d，期号=%s", request.getModeId(), request.getIssueNo())
            );
        }

        log.info("录入开奖结果成功: id={}, userId={}, modeId={}, issueNo={}",
                drawResult.getId(), userId, request.getModeId(), request.getIssueNo());

        return DrawResultVO.fromEntity(drawResult);
    }

    /**
     * 更新开奖结果（仅更新非空字段）
     *
     * @param id      开奖结果 ID
     * @param request 更新请求
     * @param userId  当前用户 ID
     * @return 更新后的开奖结果 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawResultVO update(Long id, DrawResultUpdateRequest request, Long userId) {
        DrawResult drawResult = drawResultMapper.selectById(id);
        validateOwnership(drawResult, id, userId);

        if (request.getIssueNo() != null) {
            drawResult.setIssueNo(request.getIssueNo());
        }
        if (request.getDrawDate() != null) {
            drawResult.setDrawDate(request.getDrawDate());
        }
        if (request.getRedNumbers() != null && !request.getRedNumbers().isEmpty()) {
            drawResult.setRedNumbers(joinNumbers(request.getRedNumbers()));
        }
        if (request.getBlueNumbers() != null) {
            drawResult.setBlueNumbers(request.getBlueNumbers().isEmpty()
                    ? null
                    : joinNumbers(request.getBlueNumbers()));
        }
        if (request.getPrizePool() != null) {
            drawResult.setPrizePool(request.getPrizePool());
        }

        drawResultMapper.updateById(drawResult);
        log.info("更新开奖结果成功: id={}, userId={}", id, userId);

        return DrawResultVO.fromEntity(drawResult);
    }

    /**
     * 触发中奖核对
     * 查询该期该用户所有 DrawTicket，逐条调用 WinChecker 核对，批量更新中奖状态
     *
     * @param drawResultId 开奖结果 ID
     * @param userId       当前用户 ID
     * @return 中奖核对汇总结果
     * @throws ResourceNotFoundException 若开奖结果不存在或不属于当前用户则抛出
     * @throws IllegalArgumentException  若彩票模式不支持中奖核对则抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public WinCheckSummaryVO winCheck(Long drawResultId, Long userId) {
        // 查询开奖结果
        DrawResult drawResult = drawResultMapper.selectById(drawResultId);
        validateOwnership(drawResult, drawResultId, userId);

        // 查询彩票模式
        LotteryMode mode = lotteryModeMapper.selectById(drawResult.getModeId());
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", drawResult.getModeId());
        }

        // 获取对应的 WinChecker
        WinChecker winChecker = winCheckerFactory.getChecker(mode.getCode());

        // 查询该模式的中奖规则列表（从 t_win_rule）
        List<WinRule> winRules = winRuleService.findRulesByModeId(drawResult.getModeId());

        // 解析开奖号码
        List<Integer> resultRed = parseNumbers(drawResult.getRedNumbers());
        List<Integer> resultBlue = parseNumbers(drawResult.getBlueNumbers());

        // 查询该期该用户所有 DrawTicket
        LambdaQueryWrapper<DrawTicket> wrapper = new LambdaQueryWrapper<DrawTicket>()
                .eq(DrawTicket::getUserId, userId)
                .eq(DrawTicket::getModeId, drawResult.getModeId())
                .eq(DrawTicket::getIssueNo, drawResult.getIssueNo());

        List<DrawTicket> tickets = drawTicketMapper.selectList(wrapper);

        int totalTickets = tickets.size();
        int winTickets = 0;
        Map<Integer, Integer> winLevelCounts = new HashMap<>();

        // 遍历每条 DrawTicket，执行中奖核对并更新状态
        for (DrawTicket ticket : tickets) {
            List<Integer> ticketRed = parseNumbers(ticket.getRedNumbers());
            List<Integer> ticketBlue = parseNumbers(ticket.getBlueNumbers());

            // 传入规则列表，WinChecker 优先使用规则表匹配
            WinResult winResult = winChecker.check(ticketRed, ticketBlue, resultRed, resultBlue, winRules);

            if (winResult.isWin()) {
                ticket.setWinStatus("WIN");
                ticket.setWinLevel(winResult.getWinLevel());
                // FIXED 类型自动填入固定金额，POOL 类型保留 0 待用户手动填写
                ticket.setWinAmount(winResult.getWinAmount() != null ? winResult.getWinAmount() : BigDecimal.ZERO);
                winTickets++;
                winLevelCounts.merge(winResult.getWinLevel(), 1, Integer::sum);
            } else {
                ticket.setWinStatus("NO_WIN");
                ticket.setWinLevel(null);
                ticket.setWinAmount(BigDecimal.ZERO);
            }

            drawTicketMapper.updateById(ticket);
        }

        log.info("中奖核对完成: drawResultId={}, userId={}, totalTickets={}, winTickets={}",
                drawResultId, userId, totalTickets, winTickets);

        return WinCheckSummaryVO.builder()
                .totalTickets(totalTickets)
                .winTickets(winTickets)
                .winLevelCounts(winLevelCounts)
                .build();
    }

    /**
     * 校验开奖结果归属，若不存在或不属于当前用户则抛出异常
     */
    private void validateOwnership(DrawResult result, Long id, Long userId) {
        if (result == null || !userId.equals(result.getUserId())) {
            throw new ResourceNotFoundException("开奖结果", id);
        }
    }

    /**
     * 将号码列表转为逗号分隔字符串
     */
    private String joinNumbers(List<Integer> numbers) {
        return numbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 将逗号分隔的号码字符串解析为 List<Integer>
     */
    private List<Integer> parseNumbers(String numbersStr) {
        if (numbersStr == null || numbersStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(numbersStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
