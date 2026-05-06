package com.lottery.enums;

/**
 * 数据来源枚举
 */
public enum DataSourceEnum {

    MANUAL("MANUAL"),
    API("API");

    private final String value;

    DataSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举实例
     *
     * @param value 来源字符串值
     * @return 对应的枚举，未找到则返回 null
     */
    public static DataSourceEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (DataSourceEnum source : values()) {
            if (source.value.equals(value)) {
                return source;
            }
        }
        return null;
    }
}
