package com.lottery.dto.drawresult;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 开奖结果更新请求 DTO
 * 所有字段均为可选，仅更新非空字段
 */
@Data
public class DrawResultUpdateRequest {

    /** 期号（可选） */
    private String issueNo;

    /** 开奖日期（可选） */
    private LocalDate drawDate;

    /** 开奖红球号码列表（可选） */
    private List<Integer> redNumbers;

    /** 开奖蓝球号码列表（可选） */
    private List<Integer> blueNumbers;

    /** 奖池金额（可选） */
    private BigDecimal prizePool;
}
