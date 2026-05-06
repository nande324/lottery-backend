package com.lottery.integration;

import com.lottery.common.PageResult;
import com.lottery.dto.drawticket.DrawTicketCreateRequest;
import com.lottery.dto.drawticket.DrawTicketQueryRequest;
import com.lottery.entity.LotteryMode;
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
 * 分页查询接口集成测试
 * 验证需求：需求 8.5
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaginationIntegrationTest {

    @Autowired
    private DrawTicketService drawTicketService;

    @Autowired
    private LotteryModeMapper lotteryModeMapper;

    private static final Long TEST_USER_ID = 99002L;
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

        // 插入 25 条测试记录
        for (int i = 1; i <= 25; i++) {
            DrawTicketCreateRequest req = new DrawTicketCreateRequest();
            req.setModeId(SSQ_MODE_ID);
            req.setIssueNo("2024" + String.format("%03d", i));
            req.setRedNumbers(Arrays.asList(i % 33 + 1, (i + 1) % 33 + 1, (i + 2) % 33 + 1,
                    (i + 3) % 33 + 1, (i + 4) % 33 + 1, (i + 5) % 33 + 1));
            req.setBlueNumbers(Arrays.asList(i % 16 + 1));
            req.setBetAmount(new BigDecimal("2.00"));
            req.setBetTime(LocalDateTime.now());
            drawTicketService.create(req, TEST_USER_ID);
        }
    }

    /**
     * 测试：插入 25 条记录，查询第 1 页（20条）
     */
    @Test
    void firstPageShouldReturn20Records() {
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(1);
        query.setPageSize(20);

        PageResult<DrawTicketVO> result = drawTicketService.pageQuery(TEST_USER_ID, query);

        assertEquals(25, result.getTotal(), "总记录数应为 25");
        assertEquals(1, result.getPageNum(), "当前页码应为 1");
        assertEquals(20, result.getPageSize(), "每页大小应为 20");
        assertEquals(20, result.getRecords().size(), "第1页应返回 20 条记录");
    }

    /**
     * 测试：插入 25 条记录，查询第 2 页（5条）
     */
    @Test
    void secondPageShouldReturn5Records() {
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(2);
        query.setPageSize(20);

        PageResult<DrawTicketVO> result = drawTicketService.pageQuery(TEST_USER_ID, query);

        assertEquals(25, result.getTotal(), "总记录数应为 25");
        assertEquals(2, result.getPageNum(), "当前页码应为 2");
        assertEquals(5, result.getRecords().size(), "第2页应返回 5 条记录");
    }

    /**
     * 测试：空结果时 total 应为 0
     */
    @Test
    void queryWithNoDataShouldReturnEmptyResult() {
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(1);
        query.setPageSize(20);

        // 使用不存在的用户ID查询
        PageResult<DrawTicketVO> result = drawTicketService.pageQuery(999999L, query);

        assertEquals(0, result.getTotal(), "不存在的用户应返回 total=0");
        assertTrue(result.getRecords().isEmpty(), "不存在的用户应返回空列表");
    }

    /**
     * 测试：按期号过滤分页
     */
    @Test
    void filterByIssueNoShouldReturnFilteredResults() {
        DrawTicketQueryRequest query = new DrawTicketQueryRequest();
        query.setPageNum(1);
        query.setPageSize(20);
        query.setIssueNo("2024001");

        PageResult<DrawTicketVO> result = drawTicketService.pageQuery(TEST_USER_ID, query);

        assertEquals(1, result.getTotal(), "按期号过滤应只返回1条");
        assertEquals("2024001", result.getRecords().get(0).getIssueNo());
    }
}
