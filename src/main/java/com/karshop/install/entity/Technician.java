package com.karshop.install.entity;

import com.karshop.install.enums.RegionCode;
import com.karshop.members.model.MembersVO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "technician")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_no")
    private Integer techNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private MembersVO member;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "service_area")
    private String serviceArea;

    @Column(name = "region_code", nullable = false)
    private Integer regionCodeValue = 1;

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 0;

    @Lob
    @Column(name = "tech_profile", columnDefinition = "MEDIUMBLOB")
    private byte[] techProfile;

    @Column(name = "rating_amount", nullable = false)
    private Integer ratingAmount = 0;

    @Column(name = "rating_star", nullable = false)
    private Integer ratingStar = 0;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Transient field for enum conversion
    @Transient
    public RegionCode getRegionCode() {
        return RegionCode.fromCode(regionCodeValue);
    }

    @Transient
    public void setRegionCode(RegionCode regionCode) {
        this.regionCodeValue = regionCode.getCode();
    }
}
