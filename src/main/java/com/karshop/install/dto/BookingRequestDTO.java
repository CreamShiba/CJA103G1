package com.karshop.install.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    private Integer memberNo;
    private Integer techNo;
    private Integer locationNo;
    private LocalDate appointDate;
    private Integer appointTime; // 0=morning, 1=afternoon
    private List<Integer> selectedServiceIds; // List of ts_no
}
