package com.karshop.rating.model;

import com.karshop.members.model.MembersVO;
import com.karshop.ord.model.OrdVO;
import com.karshop.sellertest.model.SellerVO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
public class RatingVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_no")
    private Integer ratingNo;

    @OneToOne
    @JoinColumn(name = "ord_no")
    private OrdVO ord;

    @ManyToOne
    @JoinColumn(name = "seller_no")
    private SellerVO seller;

    @ManyToOne
    @JoinColumn(name = "member_no")
    private MembersVO member;

    @Column(name = "rating_score")
    private Integer ratingScore;

    @Column(name = "rating_comment")
    private String ratingComment;

    @Column(name = "rating_date")
    private LocalDateTime ratingDate = LocalDateTime.now();

    public Integer getRatingNo() {
        return ratingNo;
    }

    public void setRatingNo(Integer ratingNo) {
        this.ratingNo = ratingNo;
    }

    public OrdVO getOrd() {
        return ord;
    }

    public void setOrd(OrdVO ord) {
        this.ord = ord;
    }

    public SellerVO getSeller() {
        return seller;
    }

    public void setSeller(SellerVO seller) {
        this.seller = seller;
    }

    public MembersVO getMember() {
        return member;
    }

    public void setMember(MembersVO member) {
        this.member = member;
    }

    public Integer getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(Integer ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    public LocalDateTime getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(LocalDateTime ratingDate) {
        this.ratingDate = ratingDate;
    }
}
