package com.lottery.enums;

/**
 * 中奖状态枚举
 */
public enum WinStatusEnum {

    PENDING("PENDING"),
    NO_WIN("NO_WIN"),
    WIN("WIN");

    private final String value;

    WinStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举实例
     *
     * @param value 状态字符串值
     * @return 对应的枚举，未找到则返回 null
     */
    public static WinStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (WinStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
