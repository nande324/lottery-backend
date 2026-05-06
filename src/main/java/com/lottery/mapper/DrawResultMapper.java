package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.DrawResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 开奖结果 Mapper 接口
 */
@Mapper
public interface DrawResultMapper extends BaseMapper<DrawResult> {
}
