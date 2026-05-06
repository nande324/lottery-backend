package com.lottery.integration;

import com.lottery.common.PageResult;
import com.lottery.dto.drawticket.DrawTicketCreateRequest;
import com.lottery.dto.drawticket.DrawTicketQueryRequest;
import com.lottery.entity.LotteryMode;
import com.lottery.exception.ResourceNotFoundException;
import com.lottery.mapper.LotteryModeMapper;
import com.lottery.service.DrawTicketService;
import com.lottery.vo.DrawTicketVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据隔离集成测试
 * 验证需求：需求 9.10
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataIsolationIntegrationTest {

    @Autowired
    private DrawTicketService drawTicketService;

    @Autowired
    private LotteryModeMapper lotteryModeMapper;

    private static final Long USER_A_ID = 88001L;
    private static final Long USER_B_ID = 88002L;
    private static final Long SSQ_MODE_ID = 1L;

    @BeforeEach
    void setUp() {
        // 确保双色球模式存在
        LotteryMode ssq = lotteryModeMapper.selectById(SSQ_MODE_ID);
        if (ssq == null) {
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
     * 测试：用户A创建数据，用用户B的ID查询，验证返回空列表
     */
    @Test
    void userBShouldNotSeeUserAData() {
        // 用户A创建3条摇奖票
        for (int i = 1; i <= 3; i++) {
            DrawTicketCreateRequest req = new DrawTicketCreateRequest();
            req.setModeId(SSQ_MODE_ID);
            req.setIssueNo("A-2024" + i);
            req.setRedNumbers(Arrays.asList(1, 2, 3, 4, 5, 6));
            req.setBlueNumbers(Arrays.asList(1));
            req.setBetAmount(new BigDecimal("2.00"));
            req.setBetTime(LocalDateTime.now());
            drawTicketService.create(req, USER_A_ID);
        }

        // 用用户B的ID查询
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(1);
        query.setPageSize(20);

        PageResult<DrawTicketVO> resultB = drawTicketService.pageQuery(USER_B_ID, query);

        // 验证用户B看不到用户A的数据
        assertEquals(0, resultB.getTotal(), "用户B不应看到用户A的数据，total 应为 0");
        assertTrue(resultB.getRecords().isEmpty(), "用户B的查询结果应为空列表");
    }

    /**
     * 测试：用户A能看到自己的数据
     */
    @Test
    void userAShouldSeeOwnData() {
        // 用户A创建2条摇奖票
        for (int i = 1; i <= 2; i++) {
            DrawTicketCreateRequest req = new DrawTicketCreateRequest();
            req.setModeId(SSQ_MODE_ID);
            req.setIssueNo("A-OWN-" + i);
            req.setRedNumbers(Arrays.asList(1, 2, 3, 4, 5, 6));
            req.setBlueNumbers(Arrays.asList(1));
            req.setBetAmount(new BigDecimal("2.00"));
            req.setBetTime(LocalDateTime.now());
            drawTicketService.create(req, USER_A_ID);
        }

        // 用用户A的ID查询
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(1);
        query.setPageSize(20);

        PageResult<DrawTicketVO> resultA = drawTicketService.pageQuery(USER_A_ID, query);

        assertEquals(2, resultA.getTotal(), "用户A应能看到自己的2条数据");
        assertTrue(resultA.getRecords().stream()
                .allMatch(t -> t.getUserId().equals(USER_A_ID)),
                "所有记录的 userId 应为用户A的ID");
    }

    /**
     * 测试：用户B不能访问用户A的单条记录（getById 应抛出异常）
     */
    @Test
    void userBShouldNotAccessUserATicketById() {
        // 用户A创建一条摇奖票
        DrawTicketCreateRequest req = new DrawTicketCreateRequest();
        req.setModeId(SSQ_MODE_ID);
        req.setIssueNo("A-SINGLE-001");
        req.setRedNumbers(Arrays.asList(1, 2, 3, 4, 5, 6));
        req.setBlueNumbers(Arrays.asList(1));
        req.setBetAmount(new BigDecimal("2.00"));
        req.setBetTime(LocalDateTime.now());

        DrawTicketVO ticketA = drawTicketService.create(req, USER_A_ID);
        Long ticketId = ticketA.getId();

        // 用用户B的ID尝试访问用户A的记录，应抛出 ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> drawTicketService.getById(ticketId, USER_B_ID),
                "用户B不应能访问用户A的摇奖票");
    }

    /**
     * 测试：用户B不能删除用户A的记录
     */
    @Test
    void userBShouldNotDeleteUserATicket() {
        // 用户A创建一条摇奖票
        DrawTicketCreateRequest req = new DrawTicketCreateRequest();
        req.setModeId(SSQ_MODE_ID);
        req.setIssueNo("A-DEL-001");
        req.setRedNumbers(Arrays.asList(1, 2, 3, 4, 5, 6));
        req.setBlueNumbers(Arrays.asList(1));
        req.setBetAmount(new BigDecimal("2.00"));
        req.setBetTime(LocalDateTime.now());

        DrawTicketVO ticketA = drawTicketService.create(req, USER_A_ID);
        Long ticketId = ticketA.getId();

        // 用用户B的ID尝试删除用户A的记录，应抛出 ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> drawTicketService.softDelete(ticketId, USER_B_ID),
                "用户B不应能删除用户A的摇奖票");
    }
}
