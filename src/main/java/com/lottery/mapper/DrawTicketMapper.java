package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.DrawTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 摇奖票 Mapper 接口
 */
@Mapper
public interface DrawTicketMapper extends BaseMapper<DrawTicket> {

    /**
     * 按用户ID统计摇奖票聚合数据（总消费、总中奖、总注数、中奖注数）
     *
     * @param userId 用户ID
     * @return 包含 totalCost、totalWin、totalTickets、winTickets 的 Map
     */
    @Select("SELECT SUM(bet_amount) as totalCost, SUM(win_amount) as totalWin, " +
            "COUNT(*) as totalTickets, " +
            "SUM(CASE WHEN win_status='WIN' THEN 1 ELSE 0 END) as winTickets " +
            "FROM t_draw_ticket WHERE user_id=#{userId} AND deleted=0")
    Map<String, Object> getStatsByUserId(@Param("userId") Long userId);

    /**
     * 统计概览聚合查询（支持按模式和时间范围过滤）
     *
     * @param userId    用户ID
     * @param modeId    彩票模式ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 包含 totalCost、totalWin、totalTickets、winTickets 的 Map
     */
    Map<String, Object> getOverviewStats(@Param("userId") Long userId,
                                         @Param("modeId") Long modeId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 趋势统计查询（按日/月/年分组）
     *
     * @param userId    用户ID
     * @param modeId    彩票模式ID（可选）
     * @param groupBy   分组方式：DAY / MONTH / YEAR
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 包含 period、cost、win 的 Map 列表
     */
    List<Map<String, Object>> getTrendStats(@Param("userId") Long userId,
                                            @Param("modeId") Long modeId,
                                            @Param("groupBy") String groupBy,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 各彩票模式消费占比查询
     *
     * @param userId    用户ID
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 包含 modeId、totalCost 的 Map 列表
     */
    List<Map<String, Object>> getModeDistribution(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
