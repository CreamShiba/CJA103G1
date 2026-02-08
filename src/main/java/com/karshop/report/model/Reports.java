package com.karshop.report.model; //路徑

import jakarta.persistence.*; // 連結 JPA API，讓 @Id 等標籤可以對應到資料庫表格
import lombok.Data;          // 使用 Lombok @Data 標籤，自動產生 Getter/Setter 等
import java.time.LocalDateTime; // Java 的日期時間處理工具

@Entity //告訴Spring Boot這不只是一個普通類別,要連結到資料庫的一張表讓 JPA (Java Persistence API) 自動幫我管理這個物件
@Table(name = "reports") //告訴Java要對應到哪一張表
@Data //幫我寫好get set方法
public class Reports {

    @Id //主鍵(PK),身分證字號
    @GeneratedValue(strategy = GenerationType.IDENTITY) //告訴資料庫「這個號碼請幫我自動生成」
    @Column(name = "reports_no")
    private Integer reportsNo; // 檢舉編號

    @Column(name = "reports_type", nullable = false, length = 50)
    private String reportsType; // 檢舉類型

    // 檢舉對象，用來存被檢舉的會員帳號或文章ID
    @Column(name = "reports_target", length = 100)
    private String reportsTarget; // 檢舉對象

    @Column(name = "reports_reason", nullable = false, length = 200)
    private String reportsReason; // 檢舉原因

    @Column(name = "reports_description", nullable = false, length = 500)
    private String reportsDescription; // 檢舉描述

    @Column(name = "reports_timestamp")
    private LocalDateTime reportsTimestamp; // 檢舉時間

    @Column(name = "member_no", nullable = false)
    private Integer memberNo; // 會員編號

    @Column(name = "email", length = 100)
    private String email; // 電子信箱

    @Column(name = "status", length = 20)
    private String status; // 處理狀態

    @Column(name = "handled")
    private LocalDateTime handled; // 處理時間

    @Column(name = "adm_no", nullable = false)
    private Integer admNo; // 管理員編號

    @Column(name = "reports_response", length = 500)
    private String response; // 管理員回覆

    // ✅ 新增：商品編號（用於檢舉商品）
    @Column(name = "prod_no")
    private Integer prodNo;

    // ✅ 新增：訂單編號（用於檢舉訂單）
    @Column(name = "ord_no")
    private Integer ordNo;

    // ✅ 新增：文章編號（用於檢舉文章）
    @Column(name = "post_id")
    private Integer postId;

    // ✅ 新增：留言編號（用於檢舉留言）
    @Column(name = "comment_id")
    private Integer commentId;

    // ✅ 新增：賣家編號（用於檢舉賣家）
    @Column(name = "seller_no")
    private Integer sellerNo;
}