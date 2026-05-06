package com.lottery.vo;

import com.lottery.entity.WinRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 中奖规则视图对象
 * 用于向前端返回中奖规则信息，供手动设置中奖弹窗的等级下拉使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WinRuleVO {

    /** 主键 */
    private Long id;

    /** 彩票模式ID */
    private Long modeId;

    /** 中奖等级（1=一等奖） */
    private Integer winLevel;

    /** 等级名称（如一等奖） */
    private String levelName;

    /** 红球命中数 */
    private Integer redHit;

    /** 蓝球命中数 */
    private Integer blueHit;

    /** 命中条件补充说明 */
    private String hitCondition;

    /** 奖金类型：FIXED / POOL */
    private String prizeType;

    /** 固定奖金金额（FIXED类型有效，POOL类型为null） */
    private BigDecimal fixedAmount;

    /** 规则说明 */
    private String description;

    /**
     * 从实体转换为 VO
     */
    public static WinRuleVO fromEntity(WinRule rule) {
        if (rule == null) {
            return null;
        }
        return WinRuleVO.builder()
                .id(rule.getId())
                .modeId(rule.getModeId())
                .winLevel(rule.getWinLevel())
                .levelName(rule.getLevelName())
                .redHit(rule.getRedHit())
                .blueHit(rule.getBlueHit())
                .hitCondition(rule.getHitCondition())
                .prizeType(rule.getPrizeType())
                .fixedAmount(rule.getFixedAmount())
                .description(rule.getDescription())
                .build();
    }
}
