package com.karshop.sellertest.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "seller")
public class SellerVO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_no")
    private Integer sellerNo;

    @Column(name = "seller_name")
    private String sellerName;

    // Getter / Setter
    public Integer getSellerNo() { return sellerNo; }
    public void setSellerNo(Integer sellerNo) { this.sellerNo = sellerNo; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
}