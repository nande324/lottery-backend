package com.lottery.vo;

import com.lottery.entity.LotteryMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 彩票模式视图对象
 * 用于向前端返回彩票模式的完整信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryModeVO {

    /** 主键 */
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

    /**
     * 静态工厂方法：将 LotteryMode 实体转换为 VO
     *
     * @param mode 彩票模式实体
     * @return 彩票模式 VO
     */
    public static LotteryModeVO fromEntity(LotteryMode mode) {
        if (mode == null) {
            return null;
        }
        return LotteryModeVO.builder()
                .id(mode.getId())
                .name(mode.getName())
                .code(mode.getCode())
                .type(mode.getType())
                .redCount(mode.getRedCount())
                .redMin(mode.getRedMin())
                .redMax(mode.getRedMax())
                .blueCount(mode.getBlueCount())
                .blueMin(mode.getBlueMin())
                .blueMax(mode.getBlueMax())
                .ticketPrice(mode.getTicketPrice())
                .description(mode.getDescription())
                .sortOrder(mode.getSortOrder())
                .isPreset(mode.getIsPreset())
                .build();
    }
}
