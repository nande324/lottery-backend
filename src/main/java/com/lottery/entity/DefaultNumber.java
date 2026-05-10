package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 默认号码实体类，对应数据库表 t_default_number
 * 用于存储用户预设的默认投注号码，区分不同摇奖类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_default_number")
public class DefaultNumber {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属彩票模式ID */
    @TableField("mode_id")
    private Long modeId;

    /** 默认号码名称（如：生日号、幸运数字） */
    private String name;

    /** 红球号码，逗号分隔 */
    @TableField("red_numbers")
    private String redNumbers;

    /** 蓝球号码，逗号分隔（可为空） */
    @TableField("blue_numbers")
    private String blueNumbers;

    /** 排序权重 */
    @TableField("sort_order")
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
