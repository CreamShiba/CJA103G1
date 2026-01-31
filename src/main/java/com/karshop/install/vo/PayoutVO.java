package com.karshop.install.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutVO {

    private Integer installOrderNo;
    private String technicianName;
    private String bankAccount;
    private Integer totalPrice;
    private Integer venueFee;
    private Integer serviceFee;
    private Integer platformRevenue;
    private Integer techPayout;
    private String payoutStatus;
}
