package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 摇奖票实体类，对应数据库表 t_draw_ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_draw_ticket")
public class DrawTicket {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 彩票模式ID */
    private Long modeId;

    /** 期号 */
    private String issueNo;

    /** 红球号码（逗号分隔，有序） */
    private String redNumbers;

    /** 蓝球号码（逗号分隔） */
    private String blueNumbers;

    /** 投注金额（元） */
    private BigDecimal betAmount;

    /** 投注时间 */
    private LocalDateTime betTime;

    /** 中奖状态：PENDING/NO_WIN/WIN，默认 PENDING */
    @Builder.Default
    private String winStatus = "PENDING";

    /** 中奖等级（1=一等奖，依此类推） */
    private Integer winLevel;

    /** 中奖金额（元） */
    private BigDecimal winAmount;

    /** 开奖时间（匹配开奖结果时自动保存） */
    private LocalDateTime drawTime;

    /** 是否已兑奖：0未兑奖 1已兑奖 */
    @Builder.Default
    private Integer isClaimed = 0;

    /** 中奖号码（JSON格式存储红球和蓝球的中奖情况） */
    private String winningNumbers;

    /** 是否固定号码：0否 1是 */
    private Integer isFixed;

    /** 备注 */
    private String remark;

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
