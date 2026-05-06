package com.lottery.vo;

import com.lottery.entity.DrawResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开奖结果视图对象
 * 用于向前端返回开奖结果的完整信息
 * redNumbers/blueNumbers 以 List<Integer> 形式返回，实体中为逗号分隔字符串
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrawResultVO {

    /** 主键 */
    private Long id;

    /** 录入用户 ID */
    private Long userId;

    /** 彩票模式 ID */
    private Long modeId;

    /** 期号 */
    private String issueNo;

    /** 开奖日期 */
    private LocalDate drawDate;

    /** 开奖红球号码列表 */
    private List<Integer> redNumbers;

    /** 开奖蓝球号码列表 */
    private List<Integer> blueNumbers;

    /** 奖池金额（元） */
    private BigDecimal prizePool;

    /** 数据来源：MANUAL/API */
    private String source;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /**
     * 静态工厂方法：将 DrawResult 实体转换为 VO
     * 将逗号分隔的号码字符串解析为 List<Integer>
     *
     * @param result 开奖结果实体
     * @return 开奖结果 VO
     */
    public static DrawResultVO fromEntity(DrawResult result) {
        if (result == null) {
            return null;
        }
        return DrawResultVO.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .modeId(result.getModeId())
                .issueNo(result.getIssueNo())
                .drawDate(result.getDrawDate())
                .redNumbers(parseNumbers(result.getRedNumbers()))
                .blueNumbers(parseNumbers(result.getBlueNumbers()))
                .prizePool(result.getPrizePool())
                .source(result.getSource())
                .createdTime(result.getCreatedTime())
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
}
