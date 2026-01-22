package com.karshop.sellertest.model;

import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductVO;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seller")
public class SellerVO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_no")
    private Integer sellerNo;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "seller_status")
    private String sellerStatus; // 待審核, 已開通, 停權

    @Column(name = "seller_email")
    private String sellerEmail;

    @Column(name = "seller_tax_id")
    private String sellerTaxId;

//  銀行帳號 (為了之後撥款功能)
    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // mappedBy = "seller" 指的是 ProductVO 裡面的 private SellerVO seller;
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<ProductVO> product;

    // mappedBy = "seller" 指的是 OrdVO 裡面的 private SellerVO seller;
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<OrdVO>  order;

    // Getter / Setter
    public Integer getSellerNo() { return sellerNo; }
    public void setSellerNo(Integer sellerNo) { this.sellerNo = sellerNo; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(String sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getSellerTaxId() {
        return sellerTaxId;
    }

    public void setSellerTaxId(String sellerTaxId) {
        this.sellerTaxId = sellerTaxId;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<ProductVO> getProduct() {
        return product;
    }

    public void setProduct(List<ProductVO> product) {
        this.product = product;
    }

    public List<OrdVO> getOrder() {
        return order;
    }

    public void setOrder(List<OrdVO> order) {
        this.order = order;
    }
}