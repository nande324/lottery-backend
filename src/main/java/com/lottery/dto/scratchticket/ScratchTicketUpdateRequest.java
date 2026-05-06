package com.lottery.dto.scratchticket;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新刮刮乐记录请求 DTO
 * 所有字段均为可选，仅更新非空字段
 */
@Data
public class ScratchTicketUpdateRequest {

    /** 刮奖日期（可选） */
    private LocalDate scratchDate;

    /** 刮刮乐类型/名称（可选） */
    private String scratchType;

    /** 单张面额（可选，不能为负） */
    @DecimalMin(value = "0", message = "单张面额不能小于0")
    private BigDecimal unitPrice;

    /** 购买张数（可选） */
    private Integer quantity;

    /** 消费金额（可选，不能为负） */
    @DecimalMin(value = "0", message = "消费金额不能小于0")
    private BigDecimal costAmount;

    /** 中奖金额（可选，不能为负） */
    @DecimalMin(value = "0", message = "中奖金额不能小于0")
    private BigDecimal winAmount;

    /** 备注（可选） */
    private String remark;
}
