package com.lottery.property;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.entity.DrawTicket;
import com.lottery.mapper.DrawTicketMapper;
import net.jqwik.api.*;
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
 * 属性8：用户数据严格隔离
 * Feature: lottery-management-system, Property 8: 用户数据严格隔离
 * 验证需求：需求 6.1、需求 9.10
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataIsolationPropertyTest {

    @Autowired
    private DrawTicketMapper drawTicketMapper;

    @Property(tries = 20)
    void userDataShouldBeStrictlyIsolated(
            @ForAll("userIdPairs") long[] userIds
    ) {
        long userAId = userIds[0];
        long userBId = userIds[1];

        // 用户A创建数据
        DrawTicket ticketA = DrawTicket.builder()
                .userId(userAId)
                .modeId(1L)
                .issueNo("2024001")
                .redNumbers("1,7,12,18,25,33")
                .blueNumbers("8")
                .betAmount(new BigDecimal("2.00"))
                .betTime(LocalDateTime.now())
                .winStatus("PENDING")
                .isFixed(0)
                .build();
        drawTicketMapper.insert(ticketA);

        // 用用户B的ID查询，不应看到用户A的数据
        List<DrawTicket> userBTickets = drawTicketMapper.selectList(
                new LambdaQueryWrapper<DrawTicket>()
                        .eq(DrawTicket::getUserId, userBId)
        );

        // 验证用户B的查询结果中不包含用户A的记录
        boolean containsUserAData = userBTickets.stream()
                .anyMatch(t -> t.getUserId().equals(userAId));
        Assertions.assertFalse(containsUserAData,
                "用户B的查询结果不应包含用户A的数据");

        // 用用户A的ID查询，应能看到自己的数据
        List<DrawTicket> userATickets = drawTicketMapper.selectList(
                new LambdaQueryWrapper<DrawTicket>()
                        .eq(DrawTicket::getUserId, userAId)
        );
        boolean containsTicketA = userATickets.stream()
                .anyMatch(t -> t.getId().equals(ticketA.getId()));
        Assertions.assertTrue(containsTicketA,
                "用户A应能查询到自己的数据");
    }

    @Provide
    Arbitrary<long[]> userIdPairs() {
        // 生成两个不同的用户ID
        return Combinators.combine(
                Arbitraries.longs().between(10001L, 20000L),
                Arbitraries.longs().between(20001L, 30000L)
        ).as((a, b) -> new long[]{a, b});
    }
}
