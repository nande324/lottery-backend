package com.lottery.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.dto.drawresult.DrawResultCreateRequest;
import com.lottery.dto.drawticket.DrawTicketCreateRequest;
import com.lottery.entity.DrawTicket;
import com.lottery.entity.LotteryMode;
import com.lottery.mapper.DrawTicketMapper;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.service.DrawResultService;
import com.lottery.service.DrawTicketService;
import com.lottery.vo.DrawResultVO;
import com.lottery.vo.DrawTicketVO;
import com.lottery.vo.WinCheckSummaryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 摇奖票完整业务流程集成测试
 * 验证需求：需求 3.7、需求 3.9
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DrawTicketIntegrationTest {

    @Autowired
    private DrawTicketService drawTicketService;

    @Autowired
    private DrawResultService drawResultService;

    @Autowired
    private DrawTicketMapper drawTicketMapper;

    @Autowired
    private LotteryModeMapper lotteryModeMapper;

    private static final Long TEST_USER_ID = 99001L;
    private static final Long SSQ_MODE_ID = 1L; // 双色球

    @BeforeEach
    void setUp() {
        // 确保双色球模式存在（测试数据由 data.sql 初始化）
        LotteryMode ssq = lotteryModeMapper.selectById(SSQ_MODE_ID);
        if (ssq == null) {
            // 手动插入测试用彩票模式
            LotteryMode mode = LotteryMode.builder()
                    .id(SSQ_MODE_ID)
                    .name("双色球").code("SSQ").type("DRAW")
                    .redCount(6).redMin(1).redMax(33)
                    .blueCount(1).blueMin(1).blueMax(16)
                    .ticketPrice(new BigDecimal("2.00"))
                    .sortOrder(1).isPreset(1)
                    .build();
            lotteryModeMapper.insert(mode);
        }
    }

    /**
     * 测试：创建 DrawTicket → 录入 DrawResult → 触发 WinCheck → 验证中奖状态已更新
     */
    @Test
    void fullWinCheckFlowShouldUpdateTicketStatus() {
        String issueNo = "2024001";

        // 1. 创建摇奖票（双色球：6红+1蓝，故意选择与开奖号码完全不同的号码）
        DrawTicketCreateRequest ticketReq = new DrawTicketCreateRequest();
        ticketReq.setModeId(SSQ_MODE_ID);
        ticketReq.setIssueNo(issueNo);
        ticketReq.setRedNumbers(Arrays.asList(2, 4, 6, 8, 10, 12));
        ticketReq.setBlueNumbers(Arrays.asList(3));
        ticketReq.setBetAmount(new BigDecimal("2.00"));
        ticketReq.setBetTime(LocalDateTime.now());

        DrawTicketVO savedTicket = drawTicketService.create(ticketReq, TEST_USER_ID);
        assertNotNull(savedTicket.getId(), "摇奖票应有ID");
        assertEquals("PENDING", savedTicket.getWinStatus(), "初始状态应为 PENDING");

        // 2. 录入开奖结果（与投注号码完全不同，确保未中奖）
        DrawResultCreateRequest resultReq = new DrawResultCreateRequest();
        resultReq.setModeId(SSQ_MODE_ID);
        resultReq.setIssueNo(issueNo);
        resultReq.setDrawDate(LocalDate.now());
        resultReq.setRedNumbers(Arrays.asList(1, 3, 5, 7, 9, 11));
        resultReq.setBlueNumbers(Arrays.asList(5));

        DrawResultVO savedResult = drawResultService.create(resultReq, TEST_USER_ID);
        assertNotNull(savedResult.getId(), "开奖结果应有ID");

        // 3. 触发中奖核对
        WinCheckSummaryVO summary = drawResultService.winCheck(savedResult.getId(), TEST_USER_ID);
        assertNotNull(summary, "核对结果不应为null");
        assertEquals(1, summary.getTotalTickets(), "应有1注参与核对");

        // 4. 验证摇奖票状态已更新（不再是 PENDING）
        DrawTicket updatedTicket = drawTicketMapper.selectById(savedTicket.getId());
        assertNotNull(updatedTicket);
        assertNotEquals("PENDING", updatedTicket.getWinStatus(),
                "核对后状态不应再是 PENDING");
    }

    /**
     * 测试：创建摇奖票 → 录入开奖结果（6红+1蓝全中）→ 触发 WinCheck → 验证一等奖
     */
    @Test
    void winCheckShouldDetectFirstPrize() {
        String issueNo = "2024002";
        List<Integer> winningRed = Arrays.asList(1, 7, 12, 18, 25, 33);
        List<Integer> winningBlue = Arrays.asList(8);

        // 1. 创建与开奖号码完全相同的摇奖票
        DrawTicketCreateRequest ticketReq = new DrawTicketCreateRequest();
        ticketReq.setModeId(SSQ_MODE_ID);
        ticketReq.setIssueNo(issueNo);
        ticketReq.setRedNumbers(winningRed);
        ticketReq.setBlueNumbers(winningBlue);
        ticketReq.setBetAmount(new BigDecimal("2.00"));
        ticketReq.setBetTime(LocalDateTime.now());

        DrawTicketVO savedTicket = drawTicketService.create(ticketReq, TEST_USER_ID);

        // 2. 录入相同的开奖结果
        DrawResultCreateRequest resultReq = new DrawResultCreateRequest();
        resultReq.setModeId(SSQ_MODE_ID);
        resultReq.setIssueNo(issueNo);
        resultReq.setDrawDate(LocalDate.now());
        resultReq.setRedNumbers(winningRed);
        resultReq.setBlueNumbers(winningBlue);

        DrawResultVO savedResult = drawResultService.create(resultReq, TEST_USER_ID);

        // 3. 触发中奖核对
        WinCheckSummaryVO summary = drawResultService.winCheck(savedResult.getId(), TEST_USER_ID);
        assertEquals(1, summary.getTotalTickets());
        assertEquals(1, summary.getWinTickets(), "应有1注中奖");

        // 4. 验证一等奖
        DrawTicket updatedTicket = drawTicketMapper.selectById(savedTicket.getId());
        assertEquals("WIN", updatedTicket.getWinStatus(), "应为中奖状态");
        assertEquals(1, updatedTicket.getWinLevel(), "应为一等奖");
    }

    /**
     * 测试：软删除摇奖票后不应出现在查询结果中
     */
    @Test
    void softDeletedTicketShouldNotAppearInQuery() {
        DrawTicketCreateRequest ticketReq = new DrawTicketCreateRequest();
        ticketReq.setModeId(SSQ_MODE_ID);
        ticketReq.setIssueNo("2024003");
        ticketReq.setRedNumbers(Arrays.asList(1, 2, 3, 4, 5, 6));
        ticketReq.setBlueNumbers(Arrays.asList(1));
        ticketReq.setBetAmount(new BigDecimal("2.00"));
        ticketReq.setBetTime(LocalDateTime.now());

        DrawTicketVO saved = drawTicketService.create(ticketReq, TEST_USER_ID);
        Long ticketId = saved.getId();

        // 软删除
        drawTicketService.softDelete(ticketId, TEST_USER_ID);

        // 验证 MyBatis-Plus 逻辑删除过滤
        DrawTicket deleted = drawTicketMapper.selectById(ticketId);
        assertNull(deleted, "软删除后通过 selectById 不应查到记录");
    }
}
