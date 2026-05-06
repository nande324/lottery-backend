package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 彩票模式实体类，对应数据库表 t_lottery_mode
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_lottery_mode")
public class LotteryMode {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模式名称（如双色球） */
    private String name;

    /** 模式编码（如SSQ） */
    private String code;

    /** 类型：DRAW（摇奖）/ SCRATCH（刮刮乐） */
    private String type;

    /** 红球数量 */
    private Integer redCount;

    /** 红球最小值 */
    private Integer redMin;

    /** 红球最大值 */
    private Integer redMax;

    /** 蓝球数量（0表示无蓝球） */
    private Integer blueCount;

    /** 蓝球最小值 */
    private Integer blueMin;

    /** 蓝球最大值 */
    private Integer blueMax;

    /** 单注票价（元） */
    private BigDecimal ticketPrice;

    /** 规则说明 */
    private String description;

    /** 排序权重 */
    private Integer sortOrder;

    /** 是否预置模式：0否 1是 */
    private Integer isPreset;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /** 逻辑删除标志：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
