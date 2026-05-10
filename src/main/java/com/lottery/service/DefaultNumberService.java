package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.dto.defaultnumber.DefaultNumberCreateRequest;
import com.lottery.dto.defaultnumber.DefaultNumberUpdateRequest;
import com.lottery.entity.DefaultNumber;
import com.lottery.entity.LotteryMode;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.DefaultNumberMapper;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.vo.DefaultNumberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认号码服务
 * 提供默认号码的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNumberService {

    private final DefaultNumberMapper defaultNumberMapper;
    private final LotteryModeMapper lotteryModeMapper;

    /**
     * 查询所有默认号码
     *
     * @return 默认号码VO列表
     */
    public List<DefaultNumberVO> listAll() {
        List<DefaultNumber> numbers = defaultNumberMapper.selectList(
                new LambdaQueryWrapper<DefaultNumber>()
                        .orderByAsc(DefaultNumber::getSortOrder)
        );
        return numbers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据彩票模式ID查询默认号码列表
     *
     * @param modeId 彩票模式ID
     * @return 默认号码VO列表
     */
    public List<DefaultNumberVO> listByModeId(Long modeId) {
        List<DefaultNumber> numbers = defaultNumberMapper.selectList(
                new LambdaQueryWrapper<DefaultNumber>()
                        .eq(DefaultNumber::getModeId, modeId)
                        .orderByAsc(DefaultNumber::getSortOrder)
        );
        return numbers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询单个默认号码
     *
     * @param id 默认号码ID
     * @return 默认号码VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    public DefaultNumberVO getById(Long id) {
        DefaultNumber number = defaultNumberMapper.selectById(id);
        if (number == null) {
            throw new ResourceNotFoundException("默认号码", id);
        }
        return convertToVO(number);
    }

    /**
     * 新增默认号码
     *
     * @param request 创建请求
     * @return 新增后的默认号码VO
     */
    @Transactional
    public DefaultNumberVO create(DefaultNumberCreateRequest request) {
        // 验证模式是否存在
        LotteryMode mode = lotteryModeMapper.selectById(request.getModeId());
        if (mode == null) {
            throw new ResourceNotFoundException("彩票模式", request.getModeId());
        }

        DefaultNumber number = DefaultNumber.builder()
                .modeId(request.getModeId())
                .name(request.getName())
                .redNumbers(request.getRedNumbers())
                .blueNumbers(request.getBlueNumbers())
                .sortOrder(0)
                .build();

        defaultNumberMapper.insert(number);
        log.info("新增默认号码成功: id={}, name={}, modeId={}",
                number.getId(), number.getName(), number.getModeId());

        return convertToVO(number);
    }

    /**
     * 更新默认号码
     *
     * @param id      默认号码ID
     * @param request 更新请求
     * @return 更新后的默认号码VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    @Transactional
    public DefaultNumberVO update(Long id, DefaultNumberUpdateRequest request) {
        DefaultNumber number = defaultNumberMapper.selectById(id);
        if (number == null) {
            throw new ResourceNotFoundException("默认号码", id);
        }

        if (request.getName() != null) {
            number.setName(request.getName());
        }
        if (request.getRedNumbers() != null) {
            number.setRedNumbers(request.getRedNumbers());
        }
        if (request.getBlueNumbers() != null) {
            number.setBlueNumbers(request.getBlueNumbers());
        }

        defaultNumberMapper.updateById(number);
        log.info("更新默认号码成功: id={}, name={}", number.getId(), number.getName());

        return convertToVO(number);
    }

    /**
     * 删除默认号码（软删除）
     *
     * @param id 默认号码ID
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    @Transactional
    public void deleteById(Long id) {
        DefaultNumber number = defaultNumberMapper.selectById(id);
        if (number == null) {
            throw new ResourceNotFoundException("默认号码", id);
        }
        defaultNumberMapper.deleteById(id);
        log.info("删除默认号码成功: id={}", id);
    }

    /**
     * 将实体转换为VO（带模式名称）
     *
     * @param entity 实体对象
     * @return VO对象
     */
    private DefaultNumberVO convertToVO(DefaultNumber entity) {
        if (entity == null) {
            return null;
        }
        LotteryMode mode = lotteryModeMapper.selectById(entity.getModeId());
        String modeName = mode != null ? mode.getName() : null;
        return DefaultNumberVO.fromEntity(entity, modeName);
    }
}
