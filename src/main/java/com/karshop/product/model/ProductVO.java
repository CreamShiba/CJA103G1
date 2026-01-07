package com.karshop.product.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class ProductVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_no")
    Integer prodNo;

    @Column(name = "product_category_no")
    Integer productCategoryNo;

    @Column(name = "seller_no")
    Integer sellerNo;

    @Column(name = "prod_name")
    String prodName;

    @Column(name = "prod_desc")
    String prodDesc;

    @Column(name = "prod_price")
    Integer prodPrice;

    @Column(name = "prod_status")
    String prodStatus;

    @Column(name = "rating_amount")
    Integer ratingAmount;

    @Column(name = "rating_star")
    Integer ratingStar;

    public Integer getProdNo() {
        return prodNo;
    }

    public void setProdNo(Integer prodNo) {
        this.prodNo = prodNo;
    }

    public Integer getProductCategoryNo() {
        return productCategoryNo;
    }

    public void setProductCategoryNo(Integer productCategoryNo) {
        this.productCategoryNo = productCategoryNo;
    }

    public Integer getSellerNo() {
        return sellerNo;
    }

    public void setSellerNo(Integer sellerNo) {
        this.sellerNo = sellerNo;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public Integer getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(Integer prodPrice) {
        this.prodPrice = prodPrice;
    }

    public String getProdStatus() {
        return prodStatus;
    }

    public void setProdStatus(String prodStatus) {
        this.prodStatus = prodStatus;
    }

    public Integer getRatingAmount() {
        return ratingAmount;
    }

    public void setRatingAmount(Integer ratingAmount) {
        this.ratingAmount = ratingAmount;
    }

    public Integer getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(Integer ratingStar) {
        this.ratingStar = ratingStar;
    }
}
