package com.karshop.rating.model;

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

    @Column(name = "ord_no")
    private Integer ordNo;

    @Column(name = "seller_no")
    private Integer sellerNo;

    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "rating_score")
    private Integer ratingScore;

    @Column(name = "rating_comment")
    private String ratingComment;

    @Column(name = "rating_date")
    private LocalDateTime ratingDate;

    public Integer getRatingNo() {
        return ratingNo;
    }

    public void setRatingNo(Integer ratingNo) {
        this.ratingNo = ratingNo;
    }

    public Integer getOrdNo() {
        return ordNo;
    }

    public void setOrdNo(Integer ordNo) {
        this.ordNo = ordNo;
    }

    public Integer getSellerNo() {
        return sellerNo;
    }

    public void setSellerNo(Integer sellerNo) {
        this.sellerNo = sellerNo;
    }

    public Integer getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Integer memberNo) {
        this.memberNo = memberNo;
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
