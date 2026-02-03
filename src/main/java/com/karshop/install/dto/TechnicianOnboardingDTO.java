package com.karshop.install.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianOnboardingDTO {

    private Integer memberNo;
    private String realName;
    private String phone;
    private String email;
    private String serviceArea;
    private Integer regionCode;
    private String bankCode;
    private String bankAccount;
    private MultipartFile profilePhoto;

    // Service Selection
    private java.util.List<Integer> serviceIds = new java.util.ArrayList<>();
    private java.util.Map<Integer, Integer> servicePrices = new java.util.HashMap<>();
}
