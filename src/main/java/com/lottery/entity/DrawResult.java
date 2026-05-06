package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 开奖结果实体类，对应数据库表 t_draw_result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_draw_result")
public class DrawResult {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 录入用户ID（数据隔离） */
    private Long userId;

    /** 彩票模式ID */
    private Long modeId;

    /** 期号 */
    private String issueNo;

    /** 开奖日期 */
    private LocalDate drawDate;

    /** 开奖红球号码（逗号分隔，有序） */
    private String redNumbers;

    /** 开奖蓝球号码（逗号分隔） */
    private String blueNumbers;

    /** 奖池金额（元，可选） */
    private BigDecimal prizePool;

    /** 数据来源：MANUAL/API，默认 MANUAL */
    @Builder.Default
    private String source = "MANUAL";

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
