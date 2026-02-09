package com.karshop.memberInfo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "member")
public class MemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "member_account", nullable = false, length = 30)
    @NotEmpty(message = "會員帳號: 請勿空白")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "會員帳號: 只能是英文字母、數字和_ , 且長度必需在6到10之間")
    private String memberAccount;

    @Column(name = "member_password", nullable = false, length = 255)
    @Pattern(regexp = "^$|^[a-zA-Z0-9_]{4,10}$", message = "會員密碼: 只能是英文字母、數字和_ , 且長度必需在4到10之間")
    private String memberPassword;

    @Column(name = "member_name", nullable = false, length = 10) // 根據 SQL 改為 10
    @NotEmpty(message = "會員姓名: 請勿空白")
    @Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "會員姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
    private String memberName;

    @Column(name = "member_phone", nullable = false, length = 10) // 根據 SQL 改為不可為空
    @NotEmpty(message = "手機號碼: 請勿空白")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤，請輸入09開頭共10碼的數字")
    private String memberPhone;

    @Column(name = "member_email", nullable = false, length = 100)
    @NotEmpty(message = "電子郵件: 請勿空白")
    @Email(message = "電子郵件格式錯誤")
    private String memberEmail;

    @Column(name = "address", nullable = false, length = 200) // 根據 SQL 改為 200 且不可為空
    @NotEmpty(message = "地址: 請勿空白")
    @Pattern(regexp = "^[\u4e00-\u9fa50-9\\s\\-巷弄號樓,\\.]{10,200}$", message = "地址: 只能包含中文、數字、空白及「- 巷 弄 號 樓 , .」符號，且長度需在10到200字元之間")
    private String address;

    @Column(name = "registration_time", insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Timestamp registrationTime;

    @Column(name = "account_status", columnDefinition = "TINYINT DEFAULT 0") //帳號狀態: 0未認證, 1已認證, 2停權
    private Integer accountStatus;

    @Lob
    @Column(name = "member_image", columnDefinition = "MEDIUMBLOB")
    private byte[] memberImage;

    @Column(name = "member_username", nullable = false, length = 50) // 新增欄位
    @NotEmpty(message = "會員暱稱: 請勿空白")
    @Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,50}$", message = "會員暱稱: 只能是中、英文字母、數字和_ , 且長度必需在2到50之間")
    private String memberUsername;

    @Column(name = "member_login_errcount", columnDefinition = "TINYINT DEFAULT 0") // 新增欄位
    private Integer memberLoginErrcount;

    @Column(name = "member_login_errtime") // 新增欄位
    private Timestamp memberLoginErrtime;

    @Column(name = "engineer_status", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer engineerStatus;

    @Column(name = "seller_status", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer sellerStatus;

    @Column(name = "rating_amount", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer ratingAmount;

    @Column(name = "rating_star", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer ratingStar;

    // 輔助方法：計算平均評分
    public Double getAverageRating() {
        if (ratingAmount == null || ratingAmount == 0) {
            return 0.0;
        }
        return (double) ratingStar / ratingAmount;
    }
}