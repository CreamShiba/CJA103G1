package com.karshop.orddetail.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ord_detail")
public class OrdDetailVO {

    @Id
    @Column(name = "ord_no")
    private Integer ordNo;

    @Id
    @Column(name = "prod_no")
    private Integer prodNo;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Integer price;

    public Integer getOrdNo() {
        return ordNo;
    }

    public void setOrdNo(Integer ordNo) {
        this.ordNo = ordNo;
    }

    public Integer getProdNo() {
        return prodNo;
    }

    public void setProdNo(Integer prodNo) {
        this.prodNo = prodNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
