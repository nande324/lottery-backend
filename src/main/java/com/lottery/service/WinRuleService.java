package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.entity.WinRule;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.WinRuleMapper;
import com.lottery.vo.WinRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 中奖规则服务
 * 提供中奖规则的查询及管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WinRuleService {

    private final WinRuleMapper winRuleMapper;

    /**
     * 查询指定彩票模式的所有中奖规则，按等级升序排列
     *
     * @param modeId 彩票模式ID
     * @return 中奖规则列表
     */
    public List<WinRuleVO> listByModeId(Long modeId) {
        List<WinRule> rules = winRuleMapper.selectList(
                new LambdaQueryWrapper<WinRule>()
                        .eq(WinRule::getModeId, modeId)
                        .orderByAsc(WinRule::getWinLevel)
        );
        return rules.stream()
                .map(WinRuleVO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 查询所有中奖规则（可按 modeId 过滤）
     *
     * @param modeId 彩票模式ID（可为null，表示查询全部）
     * @return 中奖规则列表
     */
    public List<WinRuleVO> listAll(Long modeId) {
        LambdaQueryWrapper<WinRule> wrapper = new LambdaQueryWrapper<WinRule>()
                .eq(modeId != null, WinRule::getModeId, modeId)
                .orderByAsc(WinRule::getModeId)
                .orderByAsc(WinRule::getWinLevel);
        return winRuleMapper.selectList(wrapper).stream()
                .map(WinRuleVO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询单条中奖规则
     *
     * @param id 规则ID
     * @return 中奖规则 VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    public WinRuleVO getById(Long id) {
        WinRule rule = winRuleMapper.selectById(id);
        if (rule == null) {
            throw new ResourceNotFoundException("中奖规则", id);
        }
        return WinRuleVO.fromEntity(rule);
    }

    /**
     * 新增中奖规则（管理员操作）
     *
     * @param rule 规则实体
     * @return 创建后的规则 VO
     */
    public WinRuleVO create(WinRule rule) {
        winRuleMapper.insert(rule);
        log.info("新增中奖规则: modeId={}, winLevel={}, levelName={}", rule.getModeId(), rule.getWinLevel(), rule.getLevelName());
        return WinRuleVO.fromEntity(rule);
    }

    /**
     * 更新中奖规则（管理员操作）
     *
     * @param id   规则ID
     * @param rule 更新内容
     * @return 更新后的规则 VO
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    public WinRuleVO update(Long id, WinRule rule) {
        WinRule existing = winRuleMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("中奖规则", id);
        }
        rule.setId(id);
        winRuleMapper.updateById(rule);
        log.info("更新中奖规则: id={}", id);
        return WinRuleVO.fromEntity(winRuleMapper.selectById(id));
    }

    /**
     * 删除中奖规则（软删除，管理员操作）
     *
     * @param id 规则ID
     * @throws ResourceNotFoundException 若不存在则抛出
     */
    public void deleteById(Long id) {
        WinRule existing = winRuleMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("中奖规则", id);
        }
        winRuleMapper.deleteById(id);
        log.info("删除中奖规则: id={}", id);
    }

    /**
     * 查询指定模式的原始规则实体列表（供 WinChecker 使用）
     *
     * @param modeId 彩票模式ID
     * @return 规则实体列表，按等级升序
     */
    public List<WinRule> findRulesByModeId(Long modeId) {
        return winRuleMapper.selectList(
                new LambdaQueryWrapper<WinRule>()
                        .eq(WinRule::getModeId, modeId)
                        .orderByAsc(WinRule::getWinLevel)
        );
    }
}
