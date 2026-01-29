package com.karshop.report.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "install_appeal_images")
@Data
public class InstallAppealImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Integer imgNo;

    @Column(name = "appeals_no")
    private Integer appealsNo; // 對應主表的 ID

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}