package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.PageResult;
import com.lottery.dto.drawticket.DrawTicketCreateRequest;
import com.lottery.dto.drawticket.DrawTicketQueryRequest;
import com.lottery.dto.drawticket.DrawTicketUpdateRequest;
import com.lottery.dto.drawticket.WinStatusUpdateRequest;
import com.lottery.entity.DrawTicket;
import com.lottery.entity.LotteryMode;
import com.lottery.entity.OfficialDrawResult;
import com.lottery.entity.WinRule;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.DrawTicketMapper;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.service.wincheck.WinChecker;
import com.lottery.service.wincheck.WinCheckerFactory;
import com.lottery.service.wincheck.WinResult;
import com.lottery.util.NumberRule;
import com.lottery.util.NumberValidator;
import com.lottery.vo.DrawTicketVO;
import com.lottery.vo.TopWinVO;
import com.lottery.vo.WinningNumbersInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 摇奖票服务
 * 提供摇奖票的增删改查及中奖状态管理业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawTicketService {

    private final DrawTicketMapper drawTicketMapper;
    private final LotteryModeMapper lotteryModeMapper;
    private final NumberValidator numberValidator;
    private final OfficialDrawSyncService officialDrawSyncService;
    private final WinCheckerFactory winCheckerFactory;
    private final WinRuleService winRuleService;

    /** 兑奖期天数（默认60天） */
    private static final int CLAIM_DAYS = 60;

    /**
     * 分页查询当前用户的摇奖票列表
     * 支持 claimableOnly 过滤：仅返回兑奖期内（WIN 且开奖时间距今 ≤ 60 天）的记录
     * 支持 claimablePriority 排序：兑奖期内的中奖记录在所有页面都优先显示
     *
     * @param userId 当前用户 ID
     * @param query  查询条件
     * @return 分页结果（兑奖期内记录置顶）
     */
    public PageResult<DrawTicketVO> pageQuery(Long userId, DrawTicketQueryRequest query) {
        Page<DrawTicket> page = new Page<>(query.getPageNum(), query.getPageSize());

        LocalDateTime claimDeadline = LocalDateTime.now().minusDays(CLAIM_DAYS);

        LambdaQueryWrapper<DrawTicket> wrapper = new LambdaQueryWrapper<DrawTicket>()
                .eq(DrawTicket::getUserId, userId)
                .eq(query.getModeId() != null, DrawTicket::getModeId, query.getModeId())
                .eq(query.getIssueNo() != null && !query.getIssueNo().isEmpty(),
                        DrawTicket::getIssueNo, query.getIssueNo())
                .eq(query.getWinStatus() != null && !query.getWinStatus().isEmpty(),
                        DrawTicket::getWinStatus, query.getWinStatus());

        // claimableOnly：仅返回兑奖期内的中奖记录（基于开奖时间）
        if (Boolean.TRUE.equals(query.getClaimableOnly())) {
            wrapper.eq(DrawTicket::getWinStatus, "WIN")
                   .isNotNull(DrawTicket::getDrawTime)
                   .ge(DrawTicket::getDrawTime, claimDeadline);
        }

        // claimablePriority：使用SQL级别的排序，兑奖期内的记录在所有页面都优先显示
        if (Boolean.TRUE.equals(query.getClaimablePriority())) {
            // 兑奖期内置顶，次级排序根据 sortBy 决定
            String secondaryOrder = "draw".equals(query.getSortBy())
                    ? "draw_time IS NULL ASC, draw_time DESC, issue_no DESC"
                    : "created_time DESC";
            wrapper.last(String.format(
                "ORDER BY CASE WHEN win_status = 'WIN' AND draw_time IS NOT NULL AND draw_time >= '%s' THEN 0 ELSE 1 END, %s",
                claimDeadline.toString().replace('T', ' '), secondaryOrder
            ));
        } else if ("draw".equals(query.getSortBy())) {
            // 按开奖时间降序，draw_time 为空的排最后，再按期号降序兜底
            wrapper.last("ORDER BY draw_time IS NULL ASC, draw_time DESC, issue_no DESC");
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(DrawTicket::getCreatedTime);
        }

        Page<DrawTicket> resultPage = drawTicketMapper.selectPage(page, wrapper);

        List<DrawTicketVO> voList = resultPage.getRecords().stream()
                .map(ticket -> {
                    // 如果有期号但没有开奖时间，尝试补充开奖时间
                    if (ticket.getIssueNo() != null && !ticket.getIssueNo().trim().isEmpty() 
                            && ticket.getDrawTime() == null) {
                        fillDrawTimeByIssueNo(ticket);
                    }
                    
                    DrawTicketVO vo = enrichWithLevelName(DrawTicketVO.fromEntity(ticket));
                    // 标记是否在兑奖期内（基于开奖时间）
                    boolean claimable = "WIN".equals(ticket.getWinStatus())
                            && ticket.getDrawTime() != null
                            && ticket.getDrawTime().isAfter(claimDeadline);
                    vo.setClaimable(claimable);
                    if (claimable) {
                        vo.setClaimDeadline(ticket.getDrawTime().plusDays(CLAIM_DAYS));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), voList);
    }

    /**
     * 根据 ID 查询单条摇奖票，并校验归属用户
     *
     * @param id     摇奖票 ID
     * @param userId 当前用户 ID
     * @return 摇奖票 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawTicketVO getById(Long id, Long userId) {
        DrawTicket ticket = drawTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);
        
        // 如果有期号但没有开奖时间，尝试补充开奖时间
        if (ticket.getIssueNo() != null && !ticket.getIssueNo().trim().isEmpty() 
                && ticket.getDrawTime() == null) {
            fillDrawTimeByIssueNo(ticket);
        }
        
        return enrichWithLevelName(DrawTicketVO.fromEntity(ticket));
    }

    /**
     * 创建单条摇奖票
     *
     * @param request 创建请求
     * @param userId  当前用户 ID
     * @return 创建后的摇奖票 VO
     * @throws ResourceNotFoundException 若彩票模式不存在则抛出
     * @throws IllegalArgumentException  若号码不符合规则则抛出
     */
    public DrawTicketVO create(DrawTicketCreateRequest request, Long userId) {
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

        DrawTicket ticket = DrawTicket.builder()
                .userId(userId)
                .modeId(request.getModeId())
                .issueNo(request.getIssueNo())
                .redNumbers(redNumbersStr)
                .blueNumbers(blueNumbersStr)
                .betAmount(request.getBetAmount())
                .betTime(request.getBetTime() != null ? request.getBetTime() : LocalDateTime.now())
                .winStatus("PENDING")
                .isFixed(request.getIsFixed() != null ? request.getIsFixed() : 0)
                .remark(request.getRemark())
                .build();

        drawTicketMapper.insert(ticket);
        log.info("创建摇奖票成功: id={}, userId={}, modeId={}, issueNo={}", 
                ticket.getId(), userId, request.getModeId(), request.getIssueNo());

        // 若填写了期号，自动查询官方历史开奖数据并判断中奖
        if (request.getIssueNo() != null && !request.getIssueNo().trim().isEmpty()) {
            log.info("准备调用autoCheckWin: ticketId={}, issueNo={}", ticket.getId(), request.getIssueNo());
            autoCheckWin(ticket, mode, request.getRedNumbers(), request.getBlueNumbers());
            log.info("autoCheckWin调用完成: ticketId={}, 最终winStatus={}", ticket.getId(), ticket.getWinStatus());
        } else {
            log.debug("跳过autoCheckWin，期号为空: ticketId={}", ticket.getId());
        }

        return DrawTicketVO.fromEntity(ticket);
    }

    /**
     * 批量创建摇奖票
     *
     * @param requests 创建请求列表
     * @param userId   当前用户 ID
     * @return 创建后的摇奖票 VO 列表
     */
    public List<DrawTicketVO> createBatch(List<DrawTicketCreateRequest> requests, Long userId) {
        List<DrawTicketVO> results = new ArrayList<>();
        for (DrawTicketCreateRequest request : requests) {
            results.add(create(request, userId));
        }
        log.info("批量创建摇奖票成功: userId={}, count={}", userId, results.size());
        return results;
    }

    /**
     * 更新摇奖票（仅更新非空字段）
     *
     * @param id      摇奖票 ID
     * @param request 更新请求
     * @param userId  当前用户 ID
     * @return 更新后的摇奖票 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawTicketVO update(Long id, DrawTicketUpdateRequest request, Long userId) {
        DrawTicket ticket = drawTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        if (request.getIssueNo() != null) {
            ticket.setIssueNo(request.getIssueNo());
        }
        if (request.getBetAmount() != null) {
            ticket.setBetAmount(request.getBetAmount());
        }
        if (request.getRemark() != null) {
            ticket.setRemark(request.getRemark());
        }
        if (request.getIsFixed() != null) {
            ticket.setIsFixed(request.getIsFixed());
        }

        drawTicketMapper.updateById(ticket);
        log.info("更新摇奖票成功: id={}, userId={}", id, userId);

        return DrawTicketVO.fromEntity(ticket);
    }

    /**
     * 软删除摇奖票
     *
     * @param id     摇奖票 ID
     * @param userId 当前用户 ID
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public void softDelete(Long id, Long userId) {
        DrawTicket ticket = drawTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        drawTicketMapper.deleteById(id);
        log.info("软删除摇奖票成功: id={}, userId={}", id, userId);
    }

    /**
     * 手动更新摇奖票中奖状态
     *
     * @param id      摇奖票 ID
     * @param request 中奖状态更新请求
     * @param userId  当前用户 ID
     * @return 更新后的摇奖票 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawTicketVO updateWinStatus(Long id, WinStatusUpdateRequest request, Long userId) {
        DrawTicket ticket = drawTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        ticket.setWinStatus(request.getWinStatus());
        ticket.setWinLevel(request.getWinLevel());

        // 若状态为 WIN 且未传入金额，尝试从规则表自动填充固定金额
        if ("WIN".equals(request.getWinStatus()) && request.getWinLevel() != null) {
            if (request.getWinAmount() != null) {
                ticket.setWinAmount(request.getWinAmount());
            } else {
                // 从规则表查找该等级的固定金额
                List<WinRule> rules = winRuleService.findRulesByModeId(ticket.getModeId());
                BigDecimal autoAmount = rules.stream()
                        .filter(r -> r.getWinLevel().equals(request.getWinLevel()) && "FIXED".equals(r.getPrizeType()))
                        .map(WinRule::getFixedAmount)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                ticket.setWinAmount(autoAmount);
            }
        } else if (!"WIN".equals(request.getWinStatus())) {
            ticket.setWinAmount(BigDecimal.ZERO);
        }

        drawTicketMapper.updateById(ticket);
        log.info("更新摇奖票中奖状态成功: id={}, userId={}, winStatus={}", id, userId, request.getWinStatus());

        return enrichWithLevelName(DrawTicketVO.fromEntity(ticket));
    }

    /**
     * 更新摇奖票兑奖状态
     *
     * @param id        摇奖票 ID
     * @param isClaimed 是否已兑奖：0未兑奖 1已兑奖
     * @param userId    当前用户 ID
     * @return 更新后的摇奖票 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public DrawTicketVO updateClaimStatus(Long id, Integer isClaimed, Long userId) {
        DrawTicket ticket = drawTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        // 使用 LambdaUpdateWrapper 确保 update_time 字段被正确更新
        LambdaUpdateWrapper<DrawTicket> updateWrapper = new LambdaUpdateWrapper<DrawTicket>()
                .eq(DrawTicket::getId, id)
                .set(DrawTicket::getIsClaimed, isClaimed);
        
        drawTicketMapper.update(null, updateWrapper);
        
        // 更新内存中的对象
        ticket.setIsClaimed(isClaimed);
        
        log.info("更新摇奖票兑奖状态成功: id={}, userId={}, isClaimed={}", id, userId, isClaimed);

        return enrichWithLevelName(DrawTicketVO.fromEntity(ticket));
    }

    /**
     * 根据期号补充开奖时间
     * 为已有的摇奖票根据期号从官方开奖结果表获取开奖时间
     *
     * @param ticket 摇奖票实体
     */
    private void fillDrawTimeByIssueNo(DrawTicket ticket) {
        if (ticket.getIssueNo() == null || ticket.getIssueNo().trim().isEmpty()) {
            return;
        }

        try {
            OfficialDrawResult officialResult = officialDrawSyncService.findByIssueNo(ticket.getIssueNo());
            if (officialResult != null && officialResult.getDrawDate() != null) {
                LocalDateTime drawTime = officialResult.getDrawDate().atStartOfDay();
                
                // 计算中奖号码（如果还没有计算过）
                String winningNumbersJson = null;
                if (ticket.getWinningNumbers() == null || ticket.getWinningNumbers().trim().isEmpty()) {
                    // 解析投注号码
                    List<Integer> ticketRed = parseNumbers(ticket.getRedNumbers());
                    List<Integer> ticketBlue = parseNumbers(ticket.getBlueNumbers());
                    
                    // 解析开奖号码
                    List<Integer> resultRed = parseNumbers(officialResult.getRedNumbers());
                    List<Integer> resultBlue = parseNumbers(officialResult.getBlueNumbers());
                    
                    // 计算中奖号码
                    WinningNumbersInfo winningNumbers = calculateWinningNumbers(ticketRed, ticketBlue, resultRed, resultBlue);
                    winningNumbersJson = serializeWinningNumbers(winningNumbers);
                    
                    log.debug("为摇奖票 id={} 计算中奖号码: {}", ticket.getId(), winningNumbersJson);
                }
                
                // 使用 LambdaUpdateWrapper 确保 update_time 字段被正确更新
                LambdaUpdateWrapper<DrawTicket> updateWrapper = new LambdaUpdateWrapper<DrawTicket>()
                        .eq(DrawTicket::getId, ticket.getId())
                        .set(DrawTicket::getDrawTime, drawTime);
                
                // 如果计算了中奖号码，也一起更新
                if (winningNumbersJson != null) {
                    updateWrapper.set(DrawTicket::getWinningNumbers, winningNumbersJson);
                    ticket.setWinningNumbers(winningNumbersJson);
                }
                
                drawTicketMapper.update(null, updateWrapper);
                
                // 更新内存中的对象
                ticket.setDrawTime(drawTime);
                
                log.debug("为摇奖票 id={} 补充开奖时间: {}", ticket.getId(), drawTime);
            }
        } catch (Exception e) {
            log.warn("补充开奖时间失败: ticketId={}, issueNo={}, error={}",
                    ticket.getId(), ticket.getIssueNo(), e.getMessage());
        }
    }

    /**
     * 自动查询官方历史开奖数据并判断中奖
     * 仅对双色球（SSQ）模式生效（lotteryId=1）
     * 若官方数据中存在该期开奖结果，则自动核对并更新中奖状态
     *
     * @param ticket     已保存的摇奖票
     * @param mode       彩票模式
     * @param ticketRed  投注红球号码
     * @param ticketBlue 投注蓝球号码
     */
    private void autoCheckWin(DrawTicket ticket, LotteryMode mode,
                               List<Integer> ticketRed, List<Integer> ticketBlue) {
        try {
            log.info("开始自动中奖核对: ticketId={}, issueNo={}, mode={}", 
                    ticket.getId(), ticket.getIssueNo(), mode.getCode());
            
            // 目前仅支持双色球自动核对
            if (!"SSQ".equalsIgnoreCase(mode.getCode())) {
                log.debug("跳过自动核对，不支持的模式: {}", mode.getCode());
                return;
            }

            OfficialDrawResult officialResult = officialDrawSyncService.findByIssueNo(ticket.getIssueNo());
            if (officialResult == null) {
                log.debug("官方历史数据中未找到期号 {} 的开奖结果，跳过自动核对", ticket.getIssueNo());
                return;
            }

            log.info("找到官方开奖结果: 期号={}, 红球={}, 蓝球={}", 
                    ticket.getIssueNo(), officialResult.getRedNumbers(), officialResult.getBlueNumbers());

            // 解析官方开奖号码
            List<Integer> resultRed = parseNumbers(officialResult.getRedNumbers());
            List<Integer> resultBlue = parseNumbers(officialResult.getBlueNumbers());

            // 调用 WinChecker 核对
            WinChecker winChecker = winCheckerFactory.getChecker(mode.getCode());
            List<WinRule> winRules = winRuleService.findRulesByModeId(mode.getId());
            
            log.info("中奖核对参数: ticketRed={}, ticketBlue={}, resultRed={}, resultBlue={}, winRulesCount={}", 
                    ticketRed, ticketBlue, resultRed, resultBlue, winRules != null ? winRules.size() : 0);
            
            if (winRules != null && !winRules.isEmpty()) {
                log.debug("中奖规则详情: {}", winRules);
            } else {
                log.warn("未找到模式ID={}的中奖规则，将使用硬编码规则", mode.getId());
            }
            
            WinResult winResult;
            try {
                winResult = winChecker.check(ticketRed, ticketBlue, resultRed, resultBlue, winRules);
            } catch (Exception e) {
                log.error("中奖核对执行失败: {}", e.getMessage(), e);
                throw e;
            }

            log.info("中奖核对结果: isWin={}, winLevel={}, winAmount={}", 
                    winResult.isWin(), winResult.getWinLevel(), winResult.getWinAmount());

            // 计算中奖号码
            WinningNumbersInfo winningNumbers = calculateWinningNumbers(ticketRed, ticketBlue, resultRed, resultBlue);
            String winningNumbersJson = serializeWinningNumbers(winningNumbers);
            LocalDateTime drawTime = officialResult.getDrawDate() != null ? 
                    officialResult.getDrawDate().atStartOfDay() : null;

            // 使用 LambdaUpdateWrapper 确保 update_time 字段被正确更新
            LambdaUpdateWrapper<DrawTicket> updateWrapper = new LambdaUpdateWrapper<DrawTicket>()
                    .eq(DrawTicket::getId, ticket.getId())
                    .set(DrawTicket::getWinningNumbers, winningNumbersJson);
            
            if (drawTime != null) {
                updateWrapper.set(DrawTicket::getDrawTime, drawTime);
            }

            if (winResult.isWin()) {
                updateWrapper.set(DrawTicket::getWinStatus, "WIN")
                           .set(DrawTicket::getWinLevel, winResult.getWinLevel())
                           .set(DrawTicket::getWinAmount, winResult.getWinAmount() != null ? 
                                   winResult.getWinAmount() : BigDecimal.ZERO);
                
                // 更新内存中的对象
                ticket.setWinStatus("WIN");
                ticket.setWinLevel(winResult.getWinLevel());
                ticket.setWinAmount(winResult.getWinAmount() != null ? winResult.getWinAmount() : BigDecimal.ZERO);
                
                log.info("自动中奖核对：摇奖票 id={} 中奖！等级={}, 期号={}",
                        ticket.getId(), winResult.getWinLevel(), ticket.getIssueNo());
            } else {
                updateWrapper.set(DrawTicket::getWinStatus, "NO_WIN")
                           .set(DrawTicket::getWinLevel, null)
                           .set(DrawTicket::getWinAmount, BigDecimal.ZERO);
                
                // 更新内存中的对象
                ticket.setWinStatus("NO_WIN");
                ticket.setWinLevel(null);
                ticket.setWinAmount(BigDecimal.ZERO);
                
                log.debug("自动中奖核对：摇奖票 id={} 未中奖，期号={}", ticket.getId(), ticket.getIssueNo());
            }

            // 更新内存中的对象
            if (drawTime != null) {
                ticket.setDrawTime(drawTime);
            }
            ticket.setWinningNumbers(winningNumbersJson);

            int updateCount = drawTicketMapper.update(null, updateWrapper);
            log.info("中奖状态更新完成: ticketId={}, winStatus={}, updateCount={}", 
                    ticket.getId(), ticket.getWinStatus(), updateCount);

        } catch (Exception e) {
            // 自动核对失败不影响主流程，仅记录日志
            log.warn("自动中奖核对失败: ticketId={}, issueNo={}, error={}",
                    ticket.getId(), ticket.getIssueNo(), e.getMessage());
        }
    }

    /**
     * 查询历史最佳中奖记录 Top N
     * 支持按等级（winLevel 升序，1=一等奖最高）或金额（winAmount 降序）排序
     *
     * @param userId  当前用户 ID
     * @param sortBy  排序方式："level"（按等级）或 "amount"（按金额）
     * @param topN    返回条数（默认5，最大20）
     * @param modeId  彩票模式过滤（可为null）
     * @return Top N 中奖记录列表
     */
    public List<TopWinVO> getTopWins(Long userId, String sortBy, int topN, Long modeId) {
        int limit = Math.min(Math.max(topN, 1), 20);
        LocalDateTime claimDeadline = LocalDateTime.now().minusDays(CLAIM_DAYS);

        LambdaQueryWrapper<DrawTicket> wrapper = new LambdaQueryWrapper<DrawTicket>()
                .eq(DrawTicket::getUserId, userId)
                .eq(DrawTicket::getWinStatus, "WIN")
                .eq(modeId != null, DrawTicket::getModeId, modeId)
                .isNotNull(DrawTicket::getWinLevel);

        // 按排序方式选择 ORDER BY
        if ("amount".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(DrawTicket::getWinAmount);
        } else {
            // 按等级：winLevel 越小奖级越高
            wrapper.orderByAsc(DrawTicket::getWinLevel)
                   .orderByDesc(DrawTicket::getWinAmount);
        }

        // 取 limit 条
        Page<DrawTicket> page = new Page<>(1, limit);
        Page<DrawTicket> resultPage = drawTicketMapper.selectPage(page, wrapper);

        // 构建模式名称缓存
        Map<Long, String> modeNameCache = new java.util.HashMap<>();

        return resultPage.getRecords().stream().map(ticket -> {
            // 如果有期号但没有开奖时间，尝试补充开奖时间
            if (ticket.getIssueNo() != null && !ticket.getIssueNo().trim().isEmpty() 
                    && ticket.getDrawTime() == null) {
                fillDrawTimeByIssueNo(ticket);
            }
            
            String modeName = modeNameCache.computeIfAbsent(ticket.getModeId(), id -> {
                LotteryMode mode = lotteryModeMapper.selectById(id);
                return mode != null ? mode.getName() : "未知模式";
            });

            // 查找等级名称
            String levelName = null;
            try {
                List<WinRule> rules = winRuleService.findRulesByModeId(ticket.getModeId());
                levelName = rules.stream()
                        .filter(r -> r.getWinLevel().equals(ticket.getWinLevel()))
                        .map(WinRule::getLevelName)
                        .findFirst().orElse(null);
            } catch (Exception ignored) { }

            boolean claimable = ticket.getDrawTime() != null
                    && ticket.getDrawTime().isAfter(claimDeadline);

            return TopWinVO.builder()
                    .id(ticket.getId())
                    .modeId(ticket.getModeId())
                    .modeName(modeName)
                    .issueNo(ticket.getIssueNo())
                    .redNumbers(ticket.getRedNumbers())
                    .blueNumbers(ticket.getBlueNumbers())
                    .winLevel(ticket.getWinLevel())
                    .levelName(levelName)
                    .winAmount(ticket.getWinAmount())
                    .betTime(ticket.getBetTime())
                    .claimable(claimable)
                    .claimDeadline(claimable ? ticket.getDrawTime().plusDays(CLAIM_DAYS) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 检查是否存在重复的摇奖票
     * 重复条件：同一用户、同一模式、相同红球号码、相同蓝球号码、相同期号（如有）、相同金额
     *
     * @param request 创建请求
     * @param userId  当前用户 ID
     * @return 重复的摇奖票列表（空表示无重复）
     */
    public List<DrawTicketVO> checkDuplicate(DrawTicketCreateRequest request, Long userId) {
        String redNumbersStr = joinNumbers(request.getRedNumbers());
        String blueNumbersStr = request.getBlueNumbers() != null && !request.getBlueNumbers().isEmpty()
                ? joinNumbers(request.getBlueNumbers()) : null;

        LambdaQueryWrapper<DrawTicket> wrapper = new LambdaQueryWrapper<DrawTicket>()
                .eq(DrawTicket::getUserId, userId)
                .eq(DrawTicket::getModeId, request.getModeId())
                .eq(DrawTicket::getRedNumbers, redNumbersStr)
                .eq(blueNumbersStr != null, DrawTicket::getBlueNumbers, blueNumbersStr)
                .isNull(blueNumbersStr == null, DrawTicket::getBlueNumbers)
                .eq(request.getBetAmount() != null, DrawTicket::getBetAmount, request.getBetAmount());

        // 期号：有填则精确匹配，没填则不作为条件
        if (request.getIssueNo() != null && !request.getIssueNo().trim().isEmpty()) {
            wrapper.eq(DrawTicket::getIssueNo, request.getIssueNo());
        }

        List<DrawTicket> duplicates = drawTicketMapper.selectList(wrapper);
        return duplicates.stream()
                .map(t -> enrichWithLevelName(DrawTicketVO.fromEntity(t)))
                .collect(Collectors.toList());
    }

    /**
     * 批量补填历史摇奖票的中奖号码
     * 针对有期号、有官方开奖记录、但 winning_numbers 为空的记录，重新计算并写入
     *
     * @param userId 当前用户 ID（只处理该用户的数据）
     * @return 处理结果：{total, updated, skipped}
     */
    public Map<String, Integer> backfillWinningNumbers(Long userId) {
        // 查询该用户所有有期号的摇奖票
        List<DrawTicket> tickets = drawTicketMapper.selectList(
                new LambdaQueryWrapper<DrawTicket>()
                        .eq(DrawTicket::getUserId, userId)
                        .isNotNull(DrawTicket::getIssueNo)
                        .ne(DrawTicket::getIssueNo, "")
        );

        int total = tickets.size();
        int updated = 0;
        int skipped = 0;

        for (DrawTicket ticket : tickets) {
            // 已有中奖号码则跳过
            if (ticket.getWinningNumbers() != null && !ticket.getWinningNumbers().trim().isEmpty()) {
                skipped++;
                continue;
            }

            try {
                OfficialDrawResult officialResult = officialDrawSyncService.findByIssueNo(ticket.getIssueNo());
                if (officialResult == null) {
                    skipped++;
                    continue;
                }

                List<Integer> ticketRed  = parseNumbers(ticket.getRedNumbers());
                List<Integer> ticketBlue = parseNumbers(ticket.getBlueNumbers());
                List<Integer> resultRed  = parseNumbers(officialResult.getRedNumbers());
                List<Integer> resultBlue = parseNumbers(officialResult.getBlueNumbers());

                WinningNumbersInfo winningNumbers = calculateWinningNumbers(ticketRed, ticketBlue, resultRed, resultBlue);
                String winningNumbersJson = serializeWinningNumbers(winningNumbers);

                LocalDateTime drawTime = officialResult.getDrawDate() != null
                        ? officialResult.getDrawDate().atStartOfDay() : null;

                LambdaUpdateWrapper<DrawTicket> updateWrapper = new LambdaUpdateWrapper<DrawTicket>()
                        .eq(DrawTicket::getId, ticket.getId())
                        .set(DrawTicket::getWinningNumbers, winningNumbersJson);

                if (drawTime != null && ticket.getDrawTime() == null) {
                    updateWrapper.set(DrawTicket::getDrawTime, drawTime);
                }

                drawTicketMapper.update(null, updateWrapper);
                updated++;
                log.debug("补填中奖号码: ticketId={}, winningNumbers={}", ticket.getId(), winningNumbersJson);

            } catch (Exception e) {
                log.warn("补填中奖号码失败: ticketId={}, error={}", ticket.getId(), e.getMessage());
                skipped++;
            }
        }

        log.info("历史中奖号码补填完成: userId={}, total={}, updated={}, skipped={}", userId, total, updated, skipped);
        return Map.of("total", total, "updated", updated, "skipped", skipped);
    }

    /**
     * 为 DrawTicketVO 填充中奖等级名称（levelName）
     * 若 winLevel 不为空，从规则表查询对应的 levelName
     */
    private DrawTicketVO enrichWithLevelName(DrawTicketVO vo) {
        if (vo == null || vo.getWinLevel() == null || vo.getModeId() == null) {
            return vo;
        }
        try {
            List<WinRule> rules = winRuleService.findRulesByModeId(vo.getModeId());
            rules.stream()
                    .filter(r -> r.getWinLevel().equals(vo.getWinLevel()))
                    .findFirst()
                    .ifPresent(r -> vo.setLevelName(r.getLevelName()));
        } catch (Exception e) {
            log.warn("填充 levelName 失败: modeId={}, winLevel={}", vo.getModeId(), vo.getWinLevel());
        }
        return vo;
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

    /**
     * 校验摇奖票归属，若不存在或不属于当前用户则抛出异常
     *
     * @param ticket 摇奖票实体（可能为null）
     * @param id     摇奖票 ID（用于异常信息）
     * @param userId 当前用户 ID
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    private void validateOwnership(DrawTicket ticket, Long id, Long userId) {
        if (ticket == null || !userId.equals(ticket.getUserId())) {
            throw new ResourceNotFoundException("摇奖票", id);
        }
    }

    /**
     * 将号码列表转为逗号分隔字符串
     *
     * @param numbers 号码列表
     * @return 逗号分隔字符串（如 "1,2,3"）
     */
    private String joinNumbers(List<Integer> numbers) {
        return numbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 计算中奖号码信息
     * 比较投注号码和开奖号码，找出匹配的号码
     *
     * @param ticketRed  投注红球号码
     * @param ticketBlue 投注蓝球号码
     * @param resultRed  开奖红球号码
     * @param resultBlue 开奖蓝球号码
     * @return 中奖号码信息
     */
    private WinningNumbersInfo calculateWinningNumbers(List<Integer> ticketRed, List<Integer> ticketBlue,
                                                       List<Integer> resultRed, List<Integer> resultBlue) {
        // 计算红球中奖号码
        List<Integer> winningRedNumbers = ticketRed.stream()
                .filter(resultRed::contains)
                .collect(Collectors.toList());

        // 计算蓝球中奖号码
        List<Integer> winningBlueNumbers = ticketBlue.stream()
                .filter(resultBlue::contains)
                .collect(Collectors.toList());

        return WinningNumbersInfo.builder()
                .winningRedNumbers(winningRedNumbers)
                .winningBlueNumbers(winningBlueNumbers)
                .redCount(winningRedNumbers.size())
                .blueCount(winningBlueNumbers.size())
                .build();
    }

    /**
     * 将中奖号码信息序列化为 JSON 字符串
     *
     * @param winningNumbers 中奖号码信息
     * @return JSON 字符串，序列化失败时返回 null
     */
    private String serializeWinningNumbers(WinningNumbersInfo winningNumbers) {
        if (winningNumbers == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(winningNumbers);
        } catch (JsonProcessingException e) {
            log.warn("序列化中奖号码信息失败: {}", e.getMessage());
            return null;
        }
    }
}
