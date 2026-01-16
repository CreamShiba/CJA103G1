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
    private Integer appealsNo; //案件編號

    @Column(name = "ord_no", nullable = false)
    private Integer ordNo; //商品訂單編號

    @Column(name = "description", nullable = false)
    private String description; //描述

    @Column(name = "status", nullable = false)
    private String status; //狀態

    @Column(name = "apply_date")
    private LocalDateTime applyDate; //申請時間

    @Column(name = "process_date")
    private LocalDateTime processDate; //處理時間

    @Column(name = "updated_date")
    private LocalDateTime updatedDate; //最後更新時間

    @Column(name = "member_no", nullable = false)
    private Integer memberNo; //申訴人編號

    @Column(name ="target_member_no", nullable = false)
    private Integer targetMemberNo; //被申訴人編號

    @Column(name = "response", nullable = false)
    private String response; //管理員回覆

    @Column(name = "adm_no", nullable = false)
    private Integer admNo; //管理員編號

    @Lob
    @Column(name = "attachment")
    private byte[] attachment; //附件連結

    @Column(name = "priority", nullable = false)
    private String priority; //優先等級
}
