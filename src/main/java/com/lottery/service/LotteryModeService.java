package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.dto.lotterymode.LotteryModeCreateRequest;
import com.lottery.dto.lotterymode.LotteryModeUpdateRequest;
import com.lottery.entity.LotteryMode;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.vo.LotteryModeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 彩票模式服务
 * 提供彩票模式的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryModeService {

    private final LotteryModeMapper lotteryModeMapper;

    /**
     * 查询所有未删除的彩票模式，按 sortOrder 升序排列
     *
     * @return 彩票模式 VO 列表
     */
    public List<LotteryModeVO> listAll() {
        List<LotteryMode> modes = lotteryModeMapper.selectList(
                new LambdaQueryWrapper<LotteryMode>()
                        .orderByAsc(LotteryMode::getSortOrder)
        );
        return modes.stream()
                .map(LotteryModeVO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询单个彩票模式
     *
     * @param id 彩票模式 ID
     * @return 彩票模式 VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    public LotteryModeVO getById(Long id) {
        LotteryMode mode = lotteryModeMapper.selectById(id);
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", id);
        }
        return LotteryModeVO.fromEntity(mode);
    }

    /**
     * 新增彩票模式（仅管理员）
     *
     * @param request 创建请求
     * @return 新增后的彩票模式 VO
     */
    @PreAuthorize("hasRole('ADMIN')")
    public LotteryModeVO create(LotteryModeCreateRequest request) {
        LotteryMode mode = LotteryMode.builder()
                .name(request.getName())
                .code(request.getCode())
                .type(request.getType())
                .redCount(request.getRedCount())
                .redMin(request.getRedMin())
                .redMax(request.getRedMax())
                .blueCount(request.getBlueCount())
                .blueMin(request.getBlueMin())
                .blueMax(request.getBlueMax())
                .ticketPrice(request.getTicketPrice())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isPreset(0)
                .build();

        lotteryModeMapper.insert(mode);
        log.info("新增彩票模式成功: id={}, name={}", mode.getId(), mode.getName());

        return LotteryModeVO.fromEntity(mode);
    }

    /**
     * 更新彩票模式（仅管理员），仅更新请求中非空的字段
     *
     * @param id      彩票模式 ID
     * @param request 更新请求
     * @return 更新后的彩票模式 VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    @PreAuthorize("hasRole('ADMIN')")
    public LotteryModeVO update(Long id, LotteryModeUpdateRequest request) {
        LotteryMode mode = lotteryModeMapper.selectById(id);
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", id);
        }

        if (request.getName() != null) {
            mode.setName(request.getName());
        }
        if (request.getCode() != null) {
            mode.setCode(request.getCode());
        }
        if (request.getType() != null) {
            mode.setType(request.getType());
        }
        if (request.getRedCount() != null) {
            mode.setRedCount(request.getRedCount());
        }
        if (request.getRedMin() != null) {
            mode.setRedMin(request.getRedMin());
        }
        if (request.getRedMax() != null) {
            mode.setRedMax(request.getRedMax());
        }
        if (request.getBlueCount() != null) {
            mode.setBlueCount(request.getBlueCount());
        }
        if (request.getBlueMin() != null) {
            mode.setBlueMin(request.getBlueMin());
        }
        if (request.getBlueMax() != null) {
            mode.setBlueMax(request.getBlueMax());
        }
        if (request.getTicketPrice() != null) {
            mode.setTicketPrice(request.getTicketPrice());
        }
        if (request.getDescription() != null) {
            mode.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            mode.setSortOrder(request.getSortOrder());
        }

        lotteryModeMapper.updateById(mode);
        log.info("更新彩票模式成功: id={}, name={}", mode.getId(), mode.getName());

        return LotteryModeVO.fromEntity(mode);
    }

    /**
     * 软删除彩票模式（仅管理员）
     *
     * @param id 彩票模式 ID
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        LotteryMode mode = lotteryModeMapper.selectById(id);
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", id);
        }
        lotteryModeMapper.deleteById(id);
        log.info("软删除彩票模式成功: id={}", id);
    }
}
