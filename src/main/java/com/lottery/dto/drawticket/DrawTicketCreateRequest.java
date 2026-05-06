package com.lottery.dto.drawticket;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建摇奖票请求 DTO
 */
@Data
public class DrawTicketCreateRequest {

    /** 彩票模式 ID（必填） */
    @NotNull(message = "彩票模式不能为空")
    private Long modeId;

    /** 期号（可选） */
    private String issueNo;

    /** 红球号码列表（必填，至少一个） */
    @NotEmpty(message = "红球号码不能为空")
    private List<Integer> redNumbers;

    /** 蓝球号码列表（可选） */
    private List<Integer> blueNumbers;

    /** 投注金额（必填，不小于0） */
    @NotNull(message = "投注金额不能为空")
    @DecimalMin(value = "0", message = "投注金额不能小于0")
    private BigDecimal betAmount;

    /** 投注时间（可选，默认当前时间） */
    private LocalDateTime betTime;

    /** 是否固定号码：0否 1是（默认0） */
    private Integer isFixed = 0;

    /** 备注（可选） */
    private String remark;
}
