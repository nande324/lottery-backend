package com.lottery.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 号码生成器
 * 根据彩票模式的号码规则随机生成合法号码
 */
@Component
public class NumberGenerator {

    /**
     * 生成单注号码
     *
     * @param rule 号码规则
     * @return 包含 "red" 和 "blue" 两个键的 Map，值为号码列表
     */
    public Map<String, List<Integer>> generate(NumberRule rule) {
        Map<String, List<Integer>> result = new HashMap<>();

        // 生成红球
        List<Integer> redNumbers = generateRedNumbers(rule);
        result.put("red", redNumbers);

        // 生成蓝球（若有）
        List<Integer> blueNumbers = new ArrayList<>();
        if (rule.getBlueCount() != null && rule.getBlueCount() > 0) {
            blueNumbers = generateUniqueNumbers(rule.getBlueMin(), rule.getBlueMax(), rule.getBlueCount());
        }
        result.put("blue", blueNumbers);

        return result;
    }

    /**
     * 批量生成号码，确保每注号码互不完全相同
     *
     * @param rule  号码规则
     * @param count 需要生成的注数（1 ≤ count ≤ 100）
     * @return 去重后的号码列表
     */
    public List<Map<String, List<Integer>>> generateBatch(NumberRule rule, int count) {
        Set<String> uniqueKeys = new LinkedHashSet<>();
        List<Map<String, List<Integer>>> resultList = new ArrayList<>();

        int maxRetries = count * 10;
        int attempts = 0;

        while (uniqueKeys.size() < count && attempts < maxRetries) {
            attempts++;
            Map<String, List<Integer>> numbers = generate(rule);
            String key = serializeNumbers(numbers);
            if (uniqueKeys.add(key)) {
                resultList.add(numbers);
            }
        }

        return resultList;
    }

    /**
     * 根据规则生成红球号码列表
     *
     * @param rule 号码规则
     * @return 红球号码列表
     */
    private List<Integer> generateRedNumbers(NumberRule rule) {
        if (rule.isAllowRedDuplicate()) {
            // 排列三/五：每位独立从 [redMin, redMax] 随机选取，允许重复
            List<Integer> numbers = new ArrayList<>();
            for (int i = 0; i < rule.getRedCount(); i++) {
                numbers.add(ThreadLocalRandom.current().nextInt(rule.getRedMin(), rule.getRedMax() + 1));
            }
            return numbers;
        } else {
            // 普通模式：从 [redMin, redMax] 随机抽取 redCount 个不重复整数，升序排列
            List<Integer> numbers = generateUniqueNumbers(rule.getRedMin(), rule.getRedMax(), rule.getRedCount());
            Collections.sort(numbers);
            return numbers;
        }
    }

    /**
     * 从 [min, max] 范围内随机抽取 count 个不重复整数
     *
     * @param min   最小值（含）
     * @param max   最大值（含）
     * @param count 需要抽取的数量
     * @return 不重复整数列表
     */
    private List<Integer> generateUniqueNumbers(int min, int max, int count) {
        // 构建候选池并随机打乱，取前 count 个
        List<Integer> pool = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            pool.add(i);
        }
        Collections.shuffle(pool, ThreadLocalRandom.current());
        return new ArrayList<>(pool.subList(0, count));
    }

    /**
     * 将号码 Map 序列化为字符串，用于去重比较
     * 格式示例：双色球 "01,07,12,18,25,33+08"，无蓝球 "01,02,03"
     *
     * @param numbers 号码 Map
     * @return 序列化字符串
     */
    private String serializeNumbers(Map<String, List<Integer>> numbers) {
        List<Integer> red = numbers.getOrDefault("red", Collections.emptyList());
        List<Integer> blue = numbers.getOrDefault("blue", Collections.emptyList());

        String redStr = red.stream()
                .map(n -> String.format("%02d", n))
                .collect(Collectors.joining(","));

        if (blue.isEmpty()) {
            return redStr;
        }

        String blueStr = blue.stream()
                .map(n -> String.format("%02d", n))
                .collect(Collectors.joining(","));

        return redStr + "+" + blueStr;
    }
}
