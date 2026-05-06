package com.lottery.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 奖金类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum PrizeTypeEnum {

    /** 固定金额奖金，直接取 fixed_amount */
    FIXED("FIXED", "固定金额"),

    /** 浮动奖金（奖池相关），实际金额由用户手动填写 */
    POOL("POOL", "浮动奖池");

    private final String code;
    private final String desc;
}
