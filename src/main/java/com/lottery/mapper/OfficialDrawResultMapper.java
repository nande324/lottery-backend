package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.OfficialDrawResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 官方历史开奖结果 Mapper 接口
 */
@Mapper
public interface OfficialDrawResultMapper extends BaseMapper<OfficialDrawResult> {
}
