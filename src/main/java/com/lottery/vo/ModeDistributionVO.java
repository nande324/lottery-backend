package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 彩票模式消费占比 VO
 * 用于饼图展示各模式消费分布
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeDistributionVO {

    /** 彩票模式 ID */
    private Long modeId;

    /** 彩票模式名称 */
    private String modeName;

    /** 该模式总消费金额（元） */
    private BigDecimal totalCost;

    /** 占比百分比（0~100） */
    private double percentage;
}
