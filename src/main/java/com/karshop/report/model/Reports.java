package com.karshop.report.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
public class Reports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reports_no")
    private Integer reportsNo;

    @Column(name = "reports_type", nullable = false)
    private String reportsType;

    @Column(name = "member_no", nullable = false)
    private Integer memberNo;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "handled")
    private LocalDateTime handled;

    @Column(name = "adm_no", nullable = false)
    private Integer admNo;
}

