package com.lottery.enums;

/**
 * 用户角色枚举
 */
public enum RoleEnum {

    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举实例
     *
     * @param value 角色字符串值
     * @return 对应的枚举，未找到则返回 null
     */
    public static RoleEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }
}
