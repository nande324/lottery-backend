package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lottery.common.PageResult;
import com.lottery.dto.scratchticket.ScratchTicketCreateRequest;
import com.lottery.dto.scratchticket.ScratchTicketQueryRequest;
import com.lottery.dto.scratchticket.ScratchTicketUpdateRequest;
import com.lottery.entity.ScratchTicket;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.ScratchTicketMapper;
import com.lottery.vo.ScratchTicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 刮刮乐记录服务
 * 提供刮刮乐记录的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScratchTicketService {

    private final ScratchTicketMapper scratchTicketMapper;

    /**
     * 分页查询当前用户的刮刮乐记录列表
     *
     * @param userId 当前用户 ID
     * @param query  查询条件（startDate、endDate、scratchType 均为可选过滤）
     * @return 分页结果
     */
    public PageResult<ScratchTicketVO> pageQuery(Long userId, ScratchTicketQueryRequest query) {
        Page<ScratchTicket> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<ScratchTicket> wrapper = new LambdaQueryWrapper<ScratchTicket>()
                .eq(ScratchTicket::getUserId, userId)
                .ge(query.getStartDate() != null, ScratchTicket::getScratchDate, query.getStartDate())
                .le(query.getEndDate() != null, ScratchTicket::getScratchDate, query.getEndDate())
                .like(query.getScratchType() != null && !query.getScratchType().isEmpty(),
                        ScratchTicket::getScratchType, query.getScratchType())
                .orderByDesc(ScratchTicket::getScratchDate);

        Page<ScratchTicket> resultPage = scratchTicketMapper.selectPage(page, wrapper);

        List<ScratchTicketVO> voList = resultPage.getRecords().stream()
                .map(ScratchTicketVO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), voList);
    }

    /**
     * 创建刮刮乐记录
     *
     * @param request 创建请求
     * @param userId  当前用户 ID
     * @return 创建后的刮刮乐记录 VO
     */
    public ScratchTicketVO create(ScratchTicketCreateRequest request, Long userId) {
        // costAmount = unitPrice × quantity（自动计算）
        BigDecimal costAmount = request.getCostAmount() != null
                ? request.getCostAmount()
                : request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        ScratchTicket ticket = ScratchTicket.builder()
                .userId(userId)
                .scratchDate(request.getScratchDate())
                .scratchType(request.getScratchType())
                .unitPrice(request.getUnitPrice())
                .quantity(request.getQuantity())
                .costAmount(costAmount)
                .winAmount(request.getWinAmount() != null ? request.getWinAmount() : BigDecimal.ZERO)
                .remark(request.getRemark())
                .build();

        scratchTicketMapper.insert(ticket);
        log.info("创建刮刮乐记录成功: id={}, userId={}", ticket.getId(), userId);

        return ScratchTicketVO.fromEntity(ticket);
    }

    /**
     * 更新刮刮乐记录（仅更新非空字段）
     *
     * @param id      刮刮乐记录 ID
     * @param request 更新请求
     * @param userId  当前用户 ID
     * @return 更新后的刮刮乐记录 VO
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public ScratchTicketVO update(Long id, ScratchTicketUpdateRequest request, Long userId) {
        ScratchTicket ticket = scratchTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        if (request.getScratchDate() != null) {
            ticket.setScratchDate(request.getScratchDate());
        }
        if (request.getScratchType() != null) {
            ticket.setScratchType(request.getScratchType());
        }
        if (request.getUnitPrice() != null) {
            ticket.setUnitPrice(request.getUnitPrice());
        }
        if (request.getQuantity() != null) {
            ticket.setQuantity(request.getQuantity());
        }
        // 如果 unitPrice 或 quantity 有变化，重新计算 costAmount
        if (request.getUnitPrice() != null || request.getQuantity() != null) {
            BigDecimal price = request.getUnitPrice() != null ? request.getUnitPrice() : ticket.getUnitPrice();
            Integer qty = request.getQuantity() != null ? request.getQuantity() : ticket.getQuantity();
            if (price != null && qty != null) {
                ticket.setCostAmount(price.multiply(BigDecimal.valueOf(qty)));
            }
        } else if (request.getCostAmount() != null) {
            ticket.setCostAmount(request.getCostAmount());
        }
        if (request.getWinAmount() != null) {
            ticket.setWinAmount(request.getWinAmount());
        }
        if (request.getRemark() != null) {
            ticket.setRemark(request.getRemark());
        }

        scratchTicketMapper.updateById(ticket);
        log.info("更新刮刮乐记录成功: id={}, userId={}", id, userId);

        return ScratchTicketVO.fromEntity(ticket);
    }

    /**
     * 软删除刮刮乐记录
     *
     * @param id     刮刮乐记录 ID
     * @param userId 当前用户 ID
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    public void softDelete(Long id, Long userId) {
        ScratchTicket ticket = scratchTicketMapper.selectById(id);
        validateOwnership(ticket, id, userId);

        scratchTicketMapper.deleteById(id);
        log.info("软删除刮刮乐记录成功: id={}, userId={}", id, userId);
    }

    /**
     * 校验刮刮乐记录归属，若不存在或不属于当前用户则抛出异常
     *
     * @param ticket 刮刮乐记录实体（可能为null）
     * @param id     记录 ID（用于异常信息）
     * @param userId 当前用户 ID
     * @throws ResourceNotFoundException 若记录不存在或不属于当前用户则抛出
     */
    private void validateOwnership(ScratchTicket ticket, Long id, Long userId) {
        if (ticket == null || !userId.equals(ticket.getUserId())) {
            throw new ResourceNotFoundException("刮刮乐记录", id);
        }
    }
}
