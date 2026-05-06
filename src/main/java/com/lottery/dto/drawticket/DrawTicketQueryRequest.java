package com.lottery.dto.drawticket;

import lombok.Data;

/**
 * 摇奖票分页查询请求 DTO
 */
@Data
public class DrawTicketQueryRequest {

    /** 彩票模式 ID（可选过滤条件） */
    private Long modeId;

    /** 期号（可选过滤条件） */
    private String issueNo;

    /** 中奖状态（可选过滤条件）：PENDING/NO_WIN/WIN */
    private String winStatus;

    /**
     * 是否仅显示兑奖期内的中奖记录（可选）
     * true：只返回 winStatus=WIN 且投注时间在兑奖截止日期之内的记录
     * 兑奖期默认 60 天（从投注时间起算）
     */
    private Boolean claimableOnly;

    /**
     * 是否优先显示兑奖期内的记录（可选）
     * true：兑奖期内的记录排在前面，然后是其他记录
     */
    private Boolean claimablePriority;

    /**
     * 排序方式（可选）
     * "created"：按创建时间降序（默认）
     * "draw"：按开奖时间/期号降序（draw_time DESC，draw_time 为空的排最后）
     */
    private String sortBy;

    /** 当前页码（默认第1页） */
    private int pageNum = 1;

    /** 每页大小（默认20条） */
    private int pageSize = 20;
}
