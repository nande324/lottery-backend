package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.DefaultNumber;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 默认号码 Mapper 接口
 */
@Mapper
public interface DefaultNumberMapper extends BaseMapper<DefaultNumber> {

    /**
     * 根据彩票模式ID查询默认号码列表
     *
     * @param modeId 彩票模式ID
     * @return 默认号码列表
     */
    List<DefaultNumber> selectByModeId(@Param("modeId") Long modeId);
}
