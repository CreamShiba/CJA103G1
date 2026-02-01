package com.karshop.report.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_appeal_images")
@Data
public class ProductAppealImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Integer imgNo; // 圖片編號

    @Column(name = "appeals_no", nullable = false)
    private Integer appealsNo; // 對應商品申訴案件編號

    @Lob
    @Column(name = "image", nullable = false)
    private byte[] image; // 圖片檔案內容

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 圖片上傳時間
}