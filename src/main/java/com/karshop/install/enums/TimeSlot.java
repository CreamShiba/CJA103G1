package com.karshop.install.enums;

public enum TimeSlot {
    MORNING(0, "早上"),
    AFTERNOON(1, "下午");

    private final Integer code;
    private final String description;

    TimeSlot(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TimeSlot fromCode(Integer code) {
        for (TimeSlot slot : values()) {
            if (slot.code.equals(code)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Unknown TimeSlot code: " + code);
    }
}
