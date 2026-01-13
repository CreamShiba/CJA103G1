package com.karshop.report.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "install_appeals")
@Data
public class InstallAppeals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appeals_no")
    private Integer appealsNo;

    @Column(name = "install_order_no", nullable = false)
    private Integer installOrderNo; //安裝訂單編號

    @Column(name = "description", length = 200, nullable = false)
    private String description; //申訴描述

    @Column(name = "status", length = 100, nullable = false)
    private String status; //處理狀態

    @Column(name = "apply_date")
    private LocalDateTime applyDate; //申請時間

    @Column(name = "process_date")
    private LocalDateTime processDate; //處理時間

    @Column(name = "updated_date")
    private LocalDateTime updatedDate; //最後更新時間

    @Column(name = "member_no", nullable = false)
    private Integer memberNo; //會員編號

    @Column(name = "target_member_no", nullable = false)
    private Integer targetMemberNo; //被申訴人編號

    @Column(name = "response", length = 200)
    private String response; //管理員回覆

    @Column(name = "adm_no")
    private Integer admNo; //管理員編號

    @Lob
    @Column(name = "attachment")
    private byte[] attachment; // 對應LONGBLOB(圖片或檔案)

    @Column(name = "priority", length = 20, nullable = false)
    private String priority; //優先等級
}
