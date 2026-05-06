package com.lottery.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页响应体
 *
 * @param <T> 分页数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int pageNum;

    /** 每页大小 */
    private int pageSize;

    /** 当前页数据列表 */
    private List<T> records;

    /**
     * 静态工厂方法
     *
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param records  数据列表
     * @param <T>      数据类型
     * @return 分页响应体
     */
    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }
}
