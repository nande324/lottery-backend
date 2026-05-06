package com.lottery.dto.scratchticket;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 刮刮乐记录分页查询请求 DTO
 */
@Data
public class ScratchTicketQueryRequest {

    /** 开始日期（可选，按日期范围过滤） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（可选，按日期范围过滤） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 刮刮乐类型（可选，模糊匹配） */
    private String scratchType;

    /** 当前页码（默认第1页） */
    private int pageNum = 1;

    /** 每页大小（默认20条） */
    private int pageSize = 20;
}
