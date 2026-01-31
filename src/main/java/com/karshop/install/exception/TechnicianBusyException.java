package com.karshop.install.exception;

public class TechnicianBusyException extends RuntimeException {

    public TechnicianBusyException(String message) {
        super(message);
    }

    public TechnicianBusyException(String message, Throwable cause) {
        super(message, cause);
    }
}
