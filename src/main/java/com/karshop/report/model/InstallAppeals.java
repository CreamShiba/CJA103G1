package com.karshop.report.model; //路徑

import lombok.Data; //使用 Lombok @Data 標籤，自動產生 Getter/Setter 等
import jakarta.persistence.*; // 連結 JPA API，讓 @Id 等標籤可以對應到資料庫表格
import java.time.LocalDateTime; // Java 的日期時間處理工具

@Entity //告訴Spring Boot這不只是一個普通類別,要連結到資料庫的一張表讓 JPA (Java Persistence API) 自動幫我管理這個物件，
        //讓我不用寫 SQL 指令，就能直接用 Java 物件來存取資料。
@Table(name = "install_appeals") //告訴Java要對應到哪一張表
@Data //幫我寫好get set方法
public class InstallAppeals {

    @Id //主鍵(PK),身分證字號
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //告訴資料庫「這個號碼請幫我自動生成，不需要我手動輸入」。
    // 作用：這對應到 SQL 中的 AUTO_INCREMENT。
    @Column(name = "appeals_no")
    private Integer appealsNo; //案件編號

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
