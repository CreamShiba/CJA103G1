package com.karshop.install.enums;

public enum PayStatus {
    UNPAID(0, "未付款"),
    PAID(1, "已付款"),
    REFUNDED(2, "已退款");

    private final Integer code;
    private final String description;

    PayStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayStatus fromCode(Integer code) {
        for (PayStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PayStatus code: " + code);
    }
}
