package com.karshop.install.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "technician_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ts_no")
    private Integer tsNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_no", nullable = false)
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_no", nullable = false)
    private ServiceItem serviceItem;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "service_status", nullable = false)
    private Integer serviceStatus = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
