package com.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.PageResult;
import com.lottery.entity.OfficialDrawResult;
import com.lottery.mapper.OfficialDrawResultMapper;
import com.lottery.vo.OfficialDrawResultVO;
import com.lottery.vo.SyncResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方历史开奖数据同步服务
 * 从 jc.zhcw.com 接口拉取双色球历史开奖数据并保存到 t_official_draw_result 表
 *
 * 接口字段说明（实测）：
 *   issue            → 期号，如 "2026044"
 *   openTime         → 开奖日期，如 "2026-04-21"
 *   frontWinningNum  → 红球，空格分隔，如 "02 14 17 18 22 30"
 *   backWinningNum   → 蓝球，如 "01"
 *   prizePoolMoney   → 奖池金额（字符串）
 *   total            → 总记录数（顶层字段）
 *   data             → 数据数组
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfficialDrawSyncService {

    private final OfficialDrawResultMapper officialDrawResultMapper;
    private final ObjectMapper objectMapper;

    /** 每页拉取条数，接口支持较大 pageSize */
    private static final int PAGE_SIZE = 100;

    private static final String API_URL_TEMPLATE =
            "https://jc.zhcw.com/port/client_json.php" +
            "?callback=cb" +
            "&transactionType=10001001" +
            "&lotteryId=1" +
            "&issueCount=0" +
            "&startIssue=%s" +
            "&endIssue=%s" +
            "&startDate=&endDate=" +
            "&type=1" +
            "&pageNum=%d" +
            "&pageSize=%d" +
            "&tt=0.1&_=1";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── 公开方法 ──────────────────────────────────────────────────────────────

    /** 同步2022年至今的全量数据 */
    public SyncResultVO syncHistoryData() {
        int currentYear = LocalDate.now().getYear();
        return syncHistoryData("2022001", currentYear + "999");
    }

    /** 同步指定期号范围的数据 */
    public SyncResultVO syncHistoryData(String startIssue, String endIssue) {
        log.info("开始同步官方历史开奖数据: startIssue={}, endIssue={}", startIssue, endIssue);

        int inserted = 0, skipped = 0, failed = 0;

        try {
            // 第一页：获取 total
            JsonNode firstPage = fetchPage(null, startIssue, endIssue, 1, PAGE_SIZE);
            if (firstPage == null) {
                return SyncResultVO.builder().inserted(0).skipped(0).failed(0)
                        .message("接口返回数据解析失败，请检查网络或接口是否可用").build();
            }

            int total = firstPage.path("total").asInt(0);
            if (total == 0) {
                return SyncResultVO.builder().inserted(0).skipped(0).failed(0)
                        .message("未获取到数据，请检查期号范围").build();
            }

            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            log.info("共 {} 条记录，分 {} 页拉取（每页{}条）", total, totalPages, PAGE_SIZE);

            // 处理第一页数据
            int[] r = processPage(firstPage, 1, totalPages);
            inserted += r[0]; skipped += r[1]; failed += r[2];

            // 后续页
            for (int pageNum = 2; pageNum <= totalPages; pageNum++) {
                try {
                    Thread.sleep(300); // 避免请求过快
                    JsonNode pageData = fetchPage(null, startIssue, endIssue, pageNum, PAGE_SIZE);
                    if (pageData == null) { failed++; continue; }
                    r = processPage(pageData, pageNum, totalPages);
                    inserted += r[0]; skipped += r[1]; failed += r[2];
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("第{}页同步失败: {}", pageNum, e.getMessage());
                    failed++;
                }
            }

        } catch (Exception e) {
            log.error("同步历史开奖数据失败: {}", e.getMessage(), e);
            return SyncResultVO.builder()
                    .inserted(inserted).skipped(skipped).failed(failed)
                    .message("同步过程中发生错误: " + e.getMessage()).build();
        }

        String message = String.format("同步完成：新增 %d 条，跳过 %d 条（已存在），失败 %d 条",
                inserted, skipped, failed);
        log.info(message);
        return SyncResultVO.builder()
                .inserted(inserted).skipped(skipped).failed(failed).message(message).build();
    }

    /** 分页查询本地官方历史开奖数据 */
    public PageResult<OfficialDrawResultVO> pageQuery(String issueNo, int pageNum, int pageSize) {
        Page<OfficialDrawResult> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OfficialDrawResult> wrapper = new LambdaQueryWrapper<OfficialDrawResult>()
                .eq(issueNo != null && !issueNo.isEmpty(), OfficialDrawResult::getIssueNo, issueNo)
                .orderByDesc(OfficialDrawResult::getDrawDate);
        Page<OfficialDrawResult> resultPage = officialDrawResultMapper.selectPage(page, wrapper);
        List<OfficialDrawResultVO> voList = resultPage.getRecords().stream()
                .map(OfficialDrawResultVO::fromEntity).collect(Collectors.toList());
        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    /** 根据期号查询单条官方开奖结果（供自动中奖核对使用） */
    public OfficialDrawResult findByIssueNo(String issueNo) {
        if (issueNo == null || issueNo.trim().isEmpty()) return null;
        return officialDrawResultMapper.selectOne(
                new LambdaQueryWrapper<OfficialDrawResult>()
                        .eq(OfficialDrawResult::getIssueNo, issueNo.trim())
                        .eq(OfficialDrawResult::getLotteryId, 1)
                        .last("LIMIT 1"));
    }

    // ── 私有方法 ──────────────────────────────────────────────────────────────

    /** 拉取一页数据，返回解析后的 JsonNode（顶层对象），失败返回 null */
    private JsonNode fetchPage(Object ignored, String startIssue, String endIssue,
                               int pageNum, int pageSize) {
        String url = String.format(API_URL_TEMPLATE, startIssue, endIssue, pageNum, pageSize);
        try {
            // 直接用 HttpURLConnection 绕过 RestTemplate 的 MIME 类型检查
            // 接口返回 "application/json charset=utf-8"（非标准，缺少分号），RestTemplate 会报错
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Referer", "https://www.zhcw.com/");
            conn.setRequestProperty("Accept", "*/*");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("第{}页 HTTP 状态码异常: {}", pageNum, status);
                return null;
            }

            String body = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            conn.disconnect();
            return parseJsonpResponse(body);
        } catch (Exception e) {
            log.error("拉取第{}页失败: {}", pageNum, e.getMessage());
            return null;
        }
    }

    /** 处理一页数据，返回 [inserted, skipped, failed] */
    private int[] processPage(JsonNode pageData, int pageNum, int totalPages) {
        int inserted = 0, skipped = 0, failed = 0;

        // 实测接口数据节点为 "data"
        JsonNode dataNode = pageData.path("data");
        if (dataNode.isMissingNode() || !dataNode.isArray() || dataNode.size() == 0) {
            log.warn("第{}页 data 字段为空", pageNum);
            return new int[]{0, 0, 0};
        }

        for (JsonNode item : dataNode) {
            try {
                OfficialDrawResult record = parseDrawResult(item);
                if (record == null) { failed++; continue; }
                officialDrawResultMapper.insert(record);
                inserted++;
            } catch (DuplicateKeyException e) {
                skipped++;
            } catch (Exception e) {
                log.warn("保存记录失败: {}", e.getMessage());
                failed++;
            }
        }

        log.info("第{}/{}页完成：新增{}条，跳过{}条，失败{}条", pageNum, totalPages, inserted, skipped, failed);
        return new int[]{inserted, skipped, failed};
    }

    /**
     * 解析单条开奖记录
     * 实测字段：issue / openTime / frontWinningNum / backWinningNum / prizePoolMoney
     */
    private OfficialDrawResult parseDrawResult(JsonNode item) {
        try {
            // 期号
            String issueNo = item.path("issue").asText("").trim();
            if (issueNo.isEmpty()) {
                log.warn("期号字段缺失，跳过: {}", item);
                return null;
            }

            // 开奖日期
            String dateStr = item.path("openTime").asText("").trim();
            LocalDate drawDate = parseDate(dateStr);
            if (drawDate == null) {
                log.warn("开奖日期解析失败: issueNo={}, dateStr={}", issueNo, dateStr);
                drawDate = LocalDate.now();
            }

            // 红球：frontWinningNum，空格分隔，如 "02 14 17 18 22 30"
            String redStr = item.path("frontWinningNum").asText("").trim();
            List<Integer> redNumbers = parseSpaceSeparated(redStr);
            if (redNumbers.isEmpty()) {
                log.warn("红球号码解析失败: issueNo={}, redStr={}", issueNo, redStr);
                return null;
            }

            // 蓝球：backWinningNum，如 "01"
            String blueStr = item.path("backWinningNum").asText("").trim();
            List<Integer> blueNumbers = parseSpaceSeparated(blueStr);

            // 奖池金额：prizePoolMoney（字符串数字）
            BigDecimal prizePool = null;
            String prizeStr = item.path("prizePoolMoney").asText("").trim();
            if (!prizeStr.isEmpty()) {
                try {
                    prizePool = new BigDecimal(prizeStr.replaceAll("[^0-9.]", ""));
                } catch (Exception ignored) {}
            }

            return OfficialDrawResult.builder()
                    .lotteryId(1)
                    .issueNo(issueNo)
                    .drawDate(drawDate)
                    .redNumbers(redNumbers.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")))
                    .blueNumbers(blueNumbers.isEmpty() ? null :
                            blueNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")))
                    .prizePool(prizePool)
                    .build();

        } catch (Exception e) {
            log.warn("解析开奖记录失败: {}", e.getMessage());
            return null;
        }
    }

    /** 解析空格（或逗号）分隔的号码字符串为整数列表 */
    private List<Integer> parseSpaceSeparated(String str) {
        if (str == null || str.trim().isEmpty()) return List.of();
        try {
            return Arrays.stream(str.trim().split("[\\s,]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("号码解析失败: str={}", str);
            return List.of();
        }
    }

    /** 解析 JSONP 响应：cb({...}) → JsonNode */
    private JsonNode parseJsonpResponse(String response) {
        if (response == null || response.trim().isEmpty()) return null;
        try {
            String json = response.trim();
            int start = json.indexOf('(');
            int end = json.lastIndexOf(')');
            if (start < 0 || end <= start) {
                log.warn("JSONP 格式异常: {}", json.substring(0, Math.min(200, json.length())));
                return null;
            }
            return objectMapper.readTree(json.substring(start + 1, end));
        } catch (Exception e) {
            log.error("解析 JSONP 失败: {}", e.getMessage());
            return null;
        }
    }

    /** 解析日期字符串，支持 yyyy-MM-dd 格式 */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim().split("\\s+")[0], DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /** 构建 RestTemplate（保留方法签名，实际不再使用） */
    private Object buildRestTemplate() {
        return null;
    }
}
