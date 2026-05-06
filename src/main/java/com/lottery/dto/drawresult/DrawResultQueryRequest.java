package com.lottery.dto.drawresult;

import lombok.Data;

/**
 * 开奖结果分页查询请求 DTO
 */
@Data
public class DrawResultQueryRequest {

    /** 彩票模式 ID（可选过滤条件） */
    private Long modeId;

    /** 期号（可选过滤条件） */
    private String issueNo;

    /** 当前页码（默认第1页） */
    private int pageNum = 1;

    /** 每页大小（默认20条） */
    private int pageSize = 20;
}
