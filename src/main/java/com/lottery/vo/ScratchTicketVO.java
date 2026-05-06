package com.lottery.vo;

import com.lottery.entity.ScratchTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 刮刮乐记录视图对象
 * 用于向前端返回刮刮乐记录的完整信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScratchTicketVO {

    /** 主键 */
    private Long id;

    /** 所属用户 ID */
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

    /** 创建时间 */
    private LocalDateTime createdTime;

    /**
     * 静态工厂方法：将 ScratchTicket 实体转换为 VO
     *
     * @param ticket 刮刮乐记录实体
     * @return 刮刮乐记录 VO
     */
    public static ScratchTicketVO fromEntity(ScratchTicket ticket) {
        if (ticket == null) {
            return null;
        }
        return ScratchTicketVO.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .scratchDate(ticket.getScratchDate())
                .scratchType(ticket.getScratchType())
                .unitPrice(ticket.getUnitPrice())
                .quantity(ticket.getQuantity())
                .costAmount(ticket.getCostAmount())
                .winAmount(ticket.getWinAmount())
                .remark(ticket.getRemark())
                .createdTime(ticket.getCreatedTime())
                .build();
    }
}
