package com.karshop.install.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "install_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstallLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_no")
    private Integer locationNo;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "price_per", nullable = false)
    private Integer pricePer;

    @Column(name = "location_status", nullable = false)
    private Integer locationStatus = 1;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();
}
