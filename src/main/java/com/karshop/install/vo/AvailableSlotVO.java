package com.karshop.install.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotVO {

    private LocalDate date;
    private Integer timeSlot; // 0=morning, 1=afternoon
    private String timeSlotDesc;
    private Integer locationNo;
    private String locationName;
    private Boolean isAvailable;
}
