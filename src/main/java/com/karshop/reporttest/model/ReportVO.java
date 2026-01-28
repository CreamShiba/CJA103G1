package com.karshop.reporttest.model;

import com.karshop.product.model.ProductVO;
import com.karshop.sellertest.model.SellerVO;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports") // 對應資料庫的 table 名稱
public class ReportVO implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reports_no")
    private Integer reportNo;

    @Column(name = "reports_type")
    private String reportType;

    @Column(name = "reports_reason")
    private String reportReason;

    @Column(name = "reports_description")
    private String reportDescription;

    @Column(name = "reports_timestamp", insertable = false, updatable = false)
    // 設定 insertable=false 代表讓資料庫自動填入 CURRENT_TIMESTAMP
    private LocalDateTime reportTime;

    @Column(name = "member_no")
    private Integer memberNo; // 檢舉人ID (這裡先用數字即可，除非你需要顯示檢舉人詳細個資)

    @Column(name = "email")
    private String email;

    @Column(name = "adm_no")
    private Integer admNo; // 處理的管理員ID

    @Column(name = "status")
    private String status = "待處理"; // Java 端也給個預設值，比較保險

    @Column(name = "handled")
    private LocalDateTime handledTime; // 處理時間

    // ===========================================
    // 🔥 重點：關聯設定 (將 prod_no 轉為 ProductVO)
    // ===========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_no", referencedColumnName = "prod_no")
    private ProductVO product;

    // 如果你有 SellerVO，建議也關聯起來，後台顯示賣家名稱會很方便
    // 如果還沒有 SellerVO，這段可以先註解掉，改用 private Integer sellerNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_no", referencedColumnName = "seller_no")
    private SellerVO seller;


    // ===========================================
    // Getter & Setter (省略 Lombok 的話需手動加入)
    // ===========================================

    public Integer getReportNo() { return reportNo; }
    public void setReportNo(Integer reportNo) { this.reportNo = reportNo; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }

    public String getReportDescription() { return reportDescription; }
    public void setReportDescription(String reportDescription) { this.reportDescription = reportDescription; }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public Integer getMemberNo() { return memberNo; }
    public void setMemberNo(Integer memberNo) { this.memberNo = memberNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAdmNo() { return admNo; }
    public void setAdmNo(Integer admNo) { this.admNo = admNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(LocalDateTime handledTime) {
        this.handledTime = handledTime;
    }

    // 🔥 重點 Getter/Setter
    public ProductVO getProduct() { return product; }
    public void setProduct(ProductVO product) { this.product = product; }

    public SellerVO getSeller() {
        return seller;
    }

    public void setSeller(SellerVO seller) {
        this.seller = seller;
    }
}