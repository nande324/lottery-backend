package com.lottery.enums;

/**
 * 彩票类型枚举
 */
public enum LotteryTypeEnum {

    DRAW("DRAW"),
    SCRATCH("SCRATCH");

    private final String value;

    LotteryTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举实例
     *
     * @param value 类型字符串值
     * @return 对应的枚举，未找到则返回 null
     */
    public static LotteryTypeEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (LotteryTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
