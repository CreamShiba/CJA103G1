package com.karshop.install.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianCardVO {

    private Integer techNo;
    private String realName;
    private String phone;
    private String email;
    private String serviceArea;
    private Double avgRating;
    private String profilePhotoBase64;
    private List<ServiceVO> services;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceVO {
        private Integer tsNo;
        private String serviceName;
        private Integer price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewVO {
        private String memberName;
        private Integer rating;
        private String comment;
        private java.time.LocalDateTime createdAt;
    }

    private List<ReviewVO> reviews;

    public void setProfilePhotoFromBytes(byte[] photoBytes) {
        if (photoBytes != null && photoBytes.length > 0) {
            this.profilePhotoBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photoBytes);
        }
    }
}
