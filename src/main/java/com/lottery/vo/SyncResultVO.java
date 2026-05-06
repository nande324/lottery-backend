package com.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据同步结果视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultVO {

    /** 本次同步新增的记录数 */
    private int inserted;

    /** 本次同步跳过（已存在）的记录数 */
    private int skipped;

    /** 同步失败的记录数 */
    private int failed;

    /** 同步说明信息 */
    private String message;
}
