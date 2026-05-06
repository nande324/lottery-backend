package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中奖规则实体类，对应数据库表 t_win_rule
 * 存储各彩票模式的中奖等级、命中条件及奖金信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_win_rule")
public class WinRule {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 彩票模式ID（关联 t_lottery_mode） */
    private Long modeId;

    /** 中奖等级（1=一等奖，数字越小奖级越高） */
    private Integer winLevel;

    /** 等级名称（如一等奖、二等奖） */
    private String levelName;

    /** 红球命中数 */
    private Integer redHit;

    /** 蓝球命中数（无蓝球模式为0） */
    private Integer blueHit;

    /** 命中条件补充说明（如"5红+1蓝 或 4红+1蓝"） */
    private String hitCondition;

    /** 奖金类型：FIXED（固定金额）/ POOL（浮动/奖池） */
    private String prizeType;

    /** 固定奖金金额（prizeType=FIXED时有效，元） */
    private BigDecimal fixedAmount;

    /** 规则说明 */
    private String description;

    /** 同模式内排序权重 */
    private Integer sortOrder;

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
