package com.karshop.buyerrating.model;

import com.karshop.members.model.MembersVO;
import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductVO;
import jakarta.persistence.Column;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prod_rate")
public class BuyerRatingVO implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_rate_no") // 請改成該表格的 Primary Key
    private Integer ratingNo;

    // 只需要關聯訂單，就可以透過訂單找到賣家
    @ManyToOne
    @JoinColumn(name = "ord_no")
    private OrdVO ord;

    @ManyToOne
    @JoinColumn(name = "prod_no")
    private ProductVO product;

    @ManyToOne
    @JoinColumn(name = "member_no")
    private MembersVO member;

    @Column(name = "rate")
    private Integer score;

    @Column(name = "rate_content")
    private String comment;

    @Column(name = "rate_time")
    private LocalDateTime ratingDate;

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

    public ProductVO getProduct() {
        return product;
    }

    public void setProduct(ProductVO product) {
        this.product = product;
    }

    public MembersVO getMember() {
        return member;
    }

    public void setMember(MembersVO member) {
        this.member = member;
    }
}
