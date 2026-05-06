package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.ScratchTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

/**
 * 刮刮乐 Mapper 接口
 */
@Mapper
public interface ScratchTicketMapper extends BaseMapper<ScratchTicket> {

    /**
     * 按用户ID统计刮刮乐聚合数据（总消费、总中奖、总注数）
     *
     * @param userId 用户ID
     * @return 包含 totalCost、totalWin、totalTickets 的 Map
     */
    @Select("SELECT SUM(cost_amount) as totalCost, SUM(win_amount) as totalWin, " +
            "COUNT(*) as totalTickets " +
            "FROM t_scratch_ticket WHERE user_id=#{userId} AND deleted=0")
    Map<String, Object> getStatsByUserId(@Param("userId") Long userId);

    /**
     * 按用户ID和时间范围统计刮刮乐聚合数据（总消费、总中奖、总注数）
     * 刮刮乐中奖注数 = win_amount > 0 的记录数
     *
     * @param userId    用户ID
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 包含 totalCost、totalWin、totalTickets、winTickets 的 Map
     */
    @Select("<script>" +
            "SELECT COALESCE(SUM(cost_amount), 0) as totalCost, " +
            "       COALESCE(SUM(win_amount), 0)  as totalWin, " +
            "       COUNT(*)                       as totalTickets, " +
            "       SUM(CASE WHEN win_amount &gt; 0 THEN 1 ELSE 0 END) as winTickets " +
            "FROM t_scratch_ticket " +
            "WHERE user_id=#{userId} AND deleted=0 " +
            "<if test='startDate != null'> AND scratch_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'>   AND scratch_date &lt;= #{endDate}   </if>" +
            "</script>")
    Map<String, Object> getOverviewStats(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * 刮刮乐趋势统计（按日/月/年分组）
     *
     * @param userId    用户ID
     * @param groupBy   分组方式：DAY / MONTH / YEAR
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 包含 period、cost、win 的 Map 列表
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(scratch_date, " +
            "  <choose>" +
            "    <when test=\"groupBy == 'DAY'\">  '%Y-%m-%d' </when>" +
            "    <when test=\"groupBy == 'YEAR'\"> '%Y'       </when>" +
            "    <otherwise>                       '%Y-%m'    </otherwise>" +
            "  </choose>" +
            ") AS period, " +
            "COALESCE(SUM(cost_amount), 0) AS cost, " +
            "COALESCE(SUM(win_amount), 0)  AS win " +
            "FROM t_scratch_ticket " +
            "WHERE user_id=#{userId} AND deleted=0 " +
            "<if test='startDate != null'> AND scratch_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'>   AND scratch_date &lt;= #{endDate}   </if>" +
            "GROUP BY period ORDER BY period ASC" +
            "</script>")
    java.util.List<Map<String, Object>> getTrendStats(
            @Param("userId") Long userId,
            @Param("groupBy") String groupBy,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询刮刮乐最早刮奖日期和首次中奖日期
     */
    @Select("<script>" +
            "SELECT " +
            "  MIN(scratch_date)                                              AS firstScratchDate, " +
            "  MIN(CASE WHEN win_amount &gt; 0 THEN scratch_date ELSE NULL END) AS firstWinDate, " +
            "  MAX(CASE WHEN win_amount &gt; 0 THEN scratch_date ELSE NULL END) AS lastWinDate, " +
            "  (SELECT scratch_date FROM t_scratch_ticket s2 " +
            "   WHERE s2.user_id=#{userId} AND s2.deleted=0 " +
            "   AND s2.win_amount = (SELECT MAX(win_amount) FROM t_scratch_ticket s3 " +
            "                        WHERE s3.user_id=#{userId} AND s3.deleted=0 " +
            "                        <if test='startDate != null'> AND s3.scratch_date &gt;= #{startDate} </if>" +
            "                        <if test='endDate != null'>   AND s3.scratch_date &lt;= #{endDate}   </if>" +
            "                       ) " +
            "   <if test='startDate != null'> AND s2.scratch_date &gt;= #{startDate} </if>" +
            "   <if test='endDate != null'>   AND s2.scratch_date &lt;= #{endDate}   </if>" +
            "   ORDER BY s2.scratch_date DESC LIMIT 1) AS maxWinDate " +
            "FROM t_scratch_ticket " +
            "WHERE user_id=#{userId} AND deleted=0 " +
            "<if test='startDate != null'> AND scratch_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'>   AND scratch_date &lt;= #{endDate}   </if>" +
            "</script>")
    Map<String, Object> getFirstDates(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 刮刮乐高级统计：总张数、最高单笔中奖、大额中奖次数
     */
    @Select("<script>" +
            "SELECT " +
            "  COALESCE(SUM(quantity), COUNT(*))                                    AS totalQuantity, " +
            "  COALESCE(MAX(win_amount), 0)                                         AS maxSingleWin, " +
            "  SUM(CASE WHEN win_amount &gt;= 1000 THEN 1 ELSE 0 END)              AS bigWinCount, " +
            "  SUM(CASE WHEN win_amount &gt; 0 THEN 1 ELSE 0 END)                  AS winCount, " +
            "  COALESCE(SUM(cost_amount), 0)                                        AS totalCost, " +
            "  COALESCE(SUM(win_amount), 0)                                         AS totalWin " +
            "FROM t_scratch_ticket " +
            "WHERE user_id=#{userId} AND deleted=0 " +
            "<if test='startDate != null'> AND scratch_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'>   AND scratch_date &lt;= #{endDate}   </if>" +
            "</script>")
    Map<String, Object> getAdvancedStats(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
