package com.karshop.report.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "install_order")
@Data
public class InstallOrderForReport {
    @Id
    @Column(name = "install_order_no")
    private Integer installOrderNo;

    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "appoint_date")
    private java.sql.Date appointDate;

    @Column(name = "order_status")
    private Integer orderStatus; // 0:未完成, 1:已完成
}