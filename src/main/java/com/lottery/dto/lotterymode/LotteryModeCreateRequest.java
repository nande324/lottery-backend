package com.lottery.dto.lotterymode;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建彩票模式请求 DTO
 */
@Data
public class LotteryModeCreateRequest {

    /** 模式名称（必填） */
    @NotBlank(message = "模式名称不能为空")
    private String name;

    /** 模式编码（必填） */
    @NotBlank(message = "模式编码不能为空")
    private String code;

    /** 类型：DRAW（摇奖）/ SCRATCH（刮刮乐）（必填） */
    @NotBlank(message = "彩票类型不能为空")
    private String type;

    /** 红球数量（可选） */
    private Integer redCount;

    /** 红球最小值（可选） */
    private Integer redMin;

    /** 红球最大值（可选） */
    private Integer redMax;

    /** 蓝球数量（可选） */
    private Integer blueCount;

    /** 蓝球最小值（可选） */
    private Integer blueMin;

    /** 蓝球最大值（可选） */
    private Integer blueMax;

    /** 单注票价（必填） */
    @NotNull(message = "票价不能为空")
    private BigDecimal ticketPrice;

    /** 规则说明（可选） */
    private String description;

    /** 排序权重（默认 0） */
    private Integer sortOrder = 0;
}
