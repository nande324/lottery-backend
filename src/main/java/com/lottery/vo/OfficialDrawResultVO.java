package com.lottery.vo;

import com.lottery.entity.OfficialDrawResult;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方历史开奖结果视图对象
 */
@Data
public class OfficialDrawResultVO {

    private Long id;
    private Integer lotteryId;
    private String issueNo;
    private LocalDate drawDate;
    /** 红球号码列表（已解析） */
    private List<Integer> redNumbers;
    /** 蓝球号码列表（已解析） */
    private List<Integer> blueNumbers;
    private BigDecimal prizePool;

    public static OfficialDrawResultVO fromEntity(OfficialDrawResult entity) {
        OfficialDrawResultVO vo = new OfficialDrawResultVO();
        vo.setId(entity.getId());
        vo.setLotteryId(entity.getLotteryId());
        vo.setIssueNo(entity.getIssueNo());
        vo.setDrawDate(entity.getDrawDate());
        vo.setRedNumbers(parseNumbers(entity.getRedNumbers()));
        vo.setBlueNumbers(parseNumbers(entity.getBlueNumbers()));
        vo.setPrizePool(entity.getPrizePool());
        return vo;
    }

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
