package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 官方历史开奖结果实体类，对应数据库表 t_official_draw_result
 * 公共表，不按用户隔离，数据从外部接口同步
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_official_draw_result")
public class OfficialDrawResult {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 彩票ID（1=双色球） */
    @Builder.Default
    private Integer lotteryId = 1;

    /** 期号 */
    private String issueNo;

    /** 开奖日期 */
    private LocalDate drawDate;

    /** 开奖红球号码（逗号分隔，升序） */
    private String redNumbers;

    /** 开奖蓝球号码（逗号分隔） */
    private String blueNumbers;

    /** 奖池金额（元，可选） */
    private BigDecimal prizePool;

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
