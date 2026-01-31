package com.karshop.install.entity;

import com.karshop.admins.model.AdminVO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_no")
    private Integer serviceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adm_no", nullable = false)
    private AdminVO adm;

    @Column(name = "service_name", nullable = false, unique = true, length = 100)
    private String serviceName;

    @Column(name = "service_desc", length = 200)
    private String serviceDesc;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
