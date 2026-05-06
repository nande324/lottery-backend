package com.lottery.dto.drawticket;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 更新中奖状态请求 DTO
 */
@Data
public class WinStatusUpdateRequest {

    /**
     * 中奖状态（必填）
     * 可选值：PENDING（待开奖）/ NO_WIN（未中奖）/ WIN（已中奖）
     */
    @NotBlank(message = "中奖状态不能为空")
    private String winStatus;

    /** 中奖等级（可选，1=一等奖，依此类推） */
    private Integer winLevel;

    /** 中奖金额（可选，元） */
    private BigDecimal winAmount;
}
