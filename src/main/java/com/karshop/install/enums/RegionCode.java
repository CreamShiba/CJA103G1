package com.karshop.install.enums;

public enum RegionCode {
    NORTH(1, "北部"),
    CENTRAL(2, "中部"),
    SOUTH(3, "南部");

    private final Integer code;
    private final String description;

    RegionCode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RegionCode fromCode(Integer code) {
        for (RegionCode region : values()) {
            if (region.code.equals(code)) {
                return region;
            }
        }
        throw new IllegalArgumentException("Unknown RegionCode: " + code);
    }
}
