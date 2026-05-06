package com.lottery.dto.drawticket;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新摇奖票请求 DTO
 * 所有字段均为可选，仅更新非空字段
 */
@Data
public class DrawTicketUpdateRequest {

    /** 期号（可选） */
    private String issueNo;

    /** 投注金额（可选） */
    private BigDecimal betAmount;

    /** 备注（可选） */
    private String remark;

    /** 是否固定号码：0否 1是（可选） */
    private Integer isFixed;
}
