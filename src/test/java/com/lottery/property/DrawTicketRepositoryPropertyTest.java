package com.lottery.property;

import com.lottery.entity.DrawTicket;
import com.lottery.mapper.DrawTicketMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.constraints.AlphaChars;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 属性4：投注记录持久化 Round-Trip
 * Feature: lottery-management-system, Property 4: 投注记录持久化 Round-Trip
 * 验证需求：需求 2.4、需求 8.1
 *
 * 注意：jqwik 与 Spring Boot Test 集成需要特殊配置
 * 此测试使用 JUnit 5 的 @SpringBootTest 注解
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DrawTicketRepositoryPropertyTest {

    @Autowired
    private DrawTicketMapper drawTicketMapper;

    @Property(tries = 20)
    void drawTicketShouldRoundTripCorrectly(
            @ForAll("validDrawTickets") DrawTicketData data
    ) {
        // 构建并保存 DrawTicket
        DrawTicket ticket = DrawTicket.builder()
                .userId(data.userId())
                .modeId(1L) // 使用固定的模式ID（双色球）
                .issueNo(data.issueNo())
                .redNumbers(data.redNumbers())
                .blueNumbers(data.blueNumbers())
                .betAmount(data.betAmount())
                .betTime(LocalDateTime.now())
                .winStatus("PENDING")
                .isFixed(0)
                .build();

        drawTicketMapper.insert(ticket);
        Long savedId = ticket.getId();
        Assertions.assertNotNull(savedId, "保存后应有主键ID");

        // 通过ID查询
        DrawTicket found = drawTicketMapper.selectById(savedId);
        Assertions.assertNotNull(found, "通过ID应能查询到记录");

        // 验证关键字段一致
        Assertions.assertEquals(data.userId(), found.getUserId(), "userId 应一致");
        Assertions.assertEquals(data.redNumbers(), found.getRedNumbers(), "redNumbers 应一致");
        Assertions.assertEquals(data.blueNumbers(), found.getBlueNumbers(), "blueNumbers 应一致");
        Assertions.assertEquals(0, data.betAmount().compareTo(found.getBetAmount()), "betAmount 应一致");
        Assertions.assertEquals("PENDING", found.getWinStatus(), "winStatus 应为 PENDING");
    }

    record DrawTicketData(
            Long userId,
            String issueNo,
            String redNumbers,
            String blueNumbers,
            BigDecimal betAmount
    ) {}

    @Provide
    Arbitrary<DrawTicketData> validDrawTickets() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(8),
                ssqRedNumbers(),
                ssqBlueNumbers(),
                Arbitraries.integers().between(2, 100).map(i -> new BigDecimal(i))
        ).as(DrawTicketData::new);
    }

    private Arbitrary<String> ssqRedNumbers() {
        return Arbitraries.integers().between(1, 33)
                .set().ofSize(6)
                .map(set -> set.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")));
    }

    private Arbitrary<String> ssqBlueNumbers() {
        return Arbitraries.integers().between(1, 16)
                .map(n -> String.valueOf(n));
    }
}
