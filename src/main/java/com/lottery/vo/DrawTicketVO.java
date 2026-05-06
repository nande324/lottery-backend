package com.lottery.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.entity.DrawTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 摇奖票视图对象
 * 用于向前端返回摇奖票的完整信息
 * redNumbers/blueNumbers 以 List<Integer> 形式返回，实体中为逗号分隔字符串
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrawTicketVO {

    /** 主键 */
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 彩票模式 ID */
    private Long modeId;

    /** 期号 */
    private String issueNo;

    /** 红球号码列表 */
    private List<Integer> redNumbers;

    /** 蓝球号码列表 */
    private List<Integer> blueNumbers;

    /** 投注金额（元） */
    private BigDecimal betAmount;

    /** 投注时间 */
    private LocalDateTime betTime;

    /** 中奖状态：PENDING/NO_WIN/WIN */
    private String winStatus;

    /** 中奖等级（1=一等奖，依此类推） */
    private Integer winLevel;

    /** 中奖等级名称（如"三等奖"，从规则表取得；未中奖为null） */
    private String levelName;

    /** 中奖金额（元） */
    private BigDecimal winAmount;

    /** 开奖时间（匹配开奖结果时自动保存） */
    private LocalDateTime drawTime;

    /** 是否已兑奖：0未兑奖 1已兑奖 */
    private Integer isClaimed;

    /** 中奖号码信息（包含红球和蓝球的中奖情况） */
    private WinningNumbersInfo winningNumbers;

    /** 是否固定号码：0否 1是 */
    private Integer isFixed;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 是否在兑奖期内（WIN 且投注时间距今 ≤ 60 天） */
    private boolean claimable;

    /** 兑奖截止日期（betTime + 60天，仅 claimable=true 时有值） */
    private LocalDateTime claimDeadline;

    /**
     * 静态工厂方法：将 DrawTicket 实体转换为 VO
     * 将逗号分隔的号码字符串解析为 List<Integer>
     *
     * @param ticket 摇奖票实体
     * @return 摇奖票 VO
     */
    public static DrawTicketVO fromEntity(DrawTicket ticket) {
        if (ticket == null) {
            return null;
        }
        return DrawTicketVO.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .modeId(ticket.getModeId())
                .issueNo(ticket.getIssueNo())
                .redNumbers(parseNumbers(ticket.getRedNumbers()))
                .blueNumbers(parseNumbers(ticket.getBlueNumbers()))
                .betAmount(ticket.getBetAmount())
                .betTime(ticket.getBetTime())
                .winStatus(ticket.getWinStatus())
                .winLevel(ticket.getWinLevel())
                .levelName(null)  // levelName 需要调用方从规则表填充，或由 Service 层注入
                .winAmount(ticket.getWinAmount())
                .drawTime(ticket.getDrawTime())
                .isClaimed(ticket.getIsClaimed())
                .winningNumbers(parseWinningNumbers(ticket.getWinningNumbers()))
                .isFixed(ticket.getIsFixed())
                .remark(ticket.getRemark())
                .createdTime(ticket.getCreatedTime())
                .build();
    }

    /**
     * 将逗号分隔的号码字符串解析为 List<Integer>
     *
     * @param numbersStr 逗号分隔的号码字符串（如 "1,2,3"）
     * @return 号码列表，若字符串为空则返回空列表
     */
    private static List<Integer> parseNumbers(String numbersStr) {
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
     * 解析中奖号码 JSON 字符串为 WinningNumbersInfo 对象
     *
     * @param winningNumbersJson JSON 字符串
     * @return WinningNumbersInfo 对象，解析失败时返回 null
     */
    private static WinningNumbersInfo parseWinningNumbers(String winningNumbersJson) {
        if (winningNumbersJson == null || winningNumbersJson.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(winningNumbersJson, WinningNumbersInfo.class);
        } catch (JsonProcessingException e) {
            log.warn("解析中奖号码 JSON 失败: {}", e.getMessage());
            return null;
        }
    }
}
