package com.karshop.report.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ord")
@Data
public class OrdForReport {
    @Id
    @Column(name = "ord_no")
    private Integer ordNo;

    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "ord_date")
    private LocalDateTime ordDate;

    @Column(name = "ord_status")
    private String ordStatus;
}