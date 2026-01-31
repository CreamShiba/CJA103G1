package com.karshop.install.entity;

import com.karshop.members.model.MembersVO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "technician_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    private Integer reviewNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "install_order_no", nullable = false)
    private InstallOrder installOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private MembersVO member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_no", nullable = false)
    private Technician technician;

    @Column(name = "rating_star", nullable = false)
    private Integer ratingStar;

    @Column(name = "review_content", length = 500)
    private String reviewContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
