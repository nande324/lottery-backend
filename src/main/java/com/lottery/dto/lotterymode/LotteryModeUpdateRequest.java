package com.lottery.dto.lotterymode;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新彩票模式请求 DTO
 * 所有字段均为可选，仅更新非空字段
 */
@Data
public class LotteryModeUpdateRequest {

    /** 模式名称 */
    private String name;

    /** 模式编码 */
    private String code;

    /** 类型：DRAW（摇奖）/ SCRATCH（刮刮乐） */
    private String type;

    /** 红球数量 */
    private Integer redCount;

    /** 红球最小值 */
    private Integer redMin;

    /** 红球最大值 */
    private Integer redMax;

    /** 蓝球数量 */
    private Integer blueCount;

    /** 蓝球最小值 */
    private Integer blueMin;

    /** 蓝球最大值 */
    private Integer blueMax;

    /** 单注票价 */
    private BigDecimal ticketPrice;

    /** 规则说明 */
    private String description;

    /** 排序权重 */
    private Integer sortOrder;
}
