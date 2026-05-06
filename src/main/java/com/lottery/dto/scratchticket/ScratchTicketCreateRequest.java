package com.lottery.dto.scratchticket;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建刮刮乐记录请求 DTO
 */
@Data
public class ScratchTicketCreateRequest {

    /** 刮奖日期（必填） */
    @NotNull(message = "刮奖日期不能为空")
    private LocalDate scratchDate;

    /** 刮刮乐类型/名称（必填） */
    @NotBlank(message = "刮刮乐类型不能为空")
    private String scratchType;

    /** 单张面额（必填，不能为负） */
    @NotNull(message = "单张面额不能为空")
    @DecimalMin(value = "0", message = "单张面额不能小于0")
    private BigDecimal unitPrice;

    /** 购买张数（必填，至少1张） */
    @NotNull(message = "购买张数不能为空")
    @javax.validation.constraints.Min(value = 1, message = "购买张数至少1张")
    private Integer quantity;

    /** 消费金额（可选，不填则由 unitPrice × quantity 自动计算） */
    @DecimalMin(value = "0", message = "消费金额不能小于0")
    private BigDecimal costAmount;

    /** 中奖金额（可选，默认0，不能为负） */
    @DecimalMin(value = "0", message = "中奖金额不能小于0")
    private BigDecimal winAmount;

    /** 备注（可选） */
    private String remark;
}
