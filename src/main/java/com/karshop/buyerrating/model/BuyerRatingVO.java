package com.karshop.buyerrating.model;

import com.karshop.ord.model.OrdVO;
import jakarta.persistence.Column;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_rating") // 🔥 請改成你實際存放買家評價的表格名稱
public class BuyerRatingVO implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_no") // 請改成該表格的 Primary Key
    private Integer ratingNo;

    // 只需要關聯訂單，就可以透過訂單找到賣家
    @OneToOne
    @JoinColumn(name = "ord_no", referencedColumnName = "ord_no")
    private OrdVO ord;

    @Column(name = "score") // 請改成實際的分數欄位名稱
    private Integer score;

    @Column(name = "comment") // 請改成實際的評論欄位名稱
    private String comment;

    @Column(name = "rating_date")
    private LocalDateTime ratingDate;

    // Getters & Setters
    public Integer getRatingNo() { return ratingNo; }
    public void setRatingNo(Integer ratingNo) { this.ratingNo = ratingNo; }
    public OrdVO getOrd() { return ord; }
    public void setOrd(OrdVO ord) { this.ord = ord; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getRatingDate() { return ratingDate; }
    public void setRatingDate(LocalDateTime ratingDate) { this.ratingDate = ratingDate; }
}
