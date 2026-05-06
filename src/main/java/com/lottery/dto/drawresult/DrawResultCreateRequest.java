package com.lottery.dto.drawresult;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 开奖结果创建请求 DTO
 */
@Data
public class DrawResultCreateRequest {

    /** 彩票模式 ID（必填） */
    @NotNull(message = "彩票模式ID不能为空")
    private Long modeId;

    /** 期号（必填） */
    @NotBlank(message = "期号不能为空")
    private String issueNo;

    /** 开奖日期（必填） */
    @NotNull(message = "开奖日期不能为空")
    private LocalDate drawDate;

    /** 开奖红球号码列表（必填） */
    @NotEmpty(message = "红球号码不能为空")
    private List<Integer> redNumbers;

    /** 开奖蓝球号码列表（可选，无蓝球模式传空） */
    private List<Integer> blueNumbers;

    /** 奖池金额（可选） */
    private BigDecimal prizePool;
}
