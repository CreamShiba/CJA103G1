package com.karshop.report.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_appeals")
@Data
public class ProductAppeals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appeals_no")
    private Integer appealsNo;

    @Column(name = "ord_no", nullable = false)
    private Integer ordNo;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "apply_date")
    private LocalDateTime applyDate;

    @Column(name = "process_date")
    private LocalDateTime processDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "member_no", nullable = false)
    private Integer memberNo;

    @Column(name ="target_member_no", nullable = false)
    private Integer targetMemberNo;

    @Column(name = "response", nullable = false)
    private String response;

    @Column(name = "adm_no", nullable = false)
    private Integer admNo;

    @Lob
    @Column(name = "attachment")
    private byte[] attachment;

    @Column(name = "priority", nullable = false)
    private String priority;
}
