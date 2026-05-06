package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.WinRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 中奖规则 Mapper 接口
 */
@Mapper
public interface WinRuleMapper extends BaseMapper<WinRule> {
}
