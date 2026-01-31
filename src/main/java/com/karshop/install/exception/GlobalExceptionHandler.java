package com.karshop.install.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingConflictException.class)
    public String handleBookingConflict(BookingConflictException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "install/error";
    }

    @ExceptionHandler(TechnicianBusyException.class)
    public String handleTechnicianBusy(TechnicianBusyException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "install/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "系統發生錯誤：" + ex.getMessage());
        return "install/error";
    }
}
