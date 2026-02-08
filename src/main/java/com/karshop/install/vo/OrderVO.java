package com.karshop.install.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    // Force recompile check

    private Integer installOrderNo;
    private String technicianName;
    private String memberName; // Added for technician view
    private String locationName;
    private LocalDate appointDate;
    private String appointTime;
    private Integer totalPrice;
    private String orderStatus;
    private String payStatus;
    private String ecpayFormHtml;
    private Integer venueFee;
    private Integer serviceFee;
    private String payoutStatus;
    private Integer techPayout; // Added for technician net income
    private boolean isReviewed;
}
