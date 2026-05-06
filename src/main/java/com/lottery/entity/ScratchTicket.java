package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 刮刮乐记录实体类，对应数据库表 t_scratch_ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_scratch_ticket")
public class ScratchTicket {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 刮奖日期 */
    private LocalDate scratchDate;

    /** 刮刮乐类型/名称 */
    private String scratchType;

    /** 单张面额（元） */
    private BigDecimal unitPrice;

    /** 购买张数 */
    private Integer quantity;

    /** 消费金额（元）= 单张面额 × 张数 */
    private BigDecimal costAmount;

    /** 中奖金额（元） */
    private BigDecimal winAmount;

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
