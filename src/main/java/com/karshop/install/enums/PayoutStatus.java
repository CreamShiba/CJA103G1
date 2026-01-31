package com.karshop.install.enums;

public enum PayoutStatus {
    PENDING(0, "待撥款"),
    COMPLETED(1, "已撥款");

    private final Integer code;
    private final String description;

    PayoutStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayoutStatus fromCode(Integer code) {
        for (PayoutStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PayoutStatus code: " + code);
    }
}
