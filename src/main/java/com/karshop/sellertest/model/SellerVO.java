package com.karshop.sellertest.model;

import com.karshop.members.model.MembersVO;
import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductVO;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seller_info")
public class SellerVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_no")
    private Integer sellerNo;

    @OneToOne
    @JoinColumn(name = "member_no")
    private MembersVO member;

    @Column(name = "seller_name")
    private String sellerName; // 負責人姓名

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String sellerEmail;

    @Column(name = "seller_tax_id")
    private String sellerTaxId;

    @Column(name = "status")
    private String sellerStatus; // 狀態: 待審核, 已通過, 停權

    @Column(name = "description")
    private String description;  // 賣場描述

    // isverified TINYINT -> Java 建議用 Boolean (0=false, 1=true)
    @Column(name = "isverified")
    private Boolean isVerified;  // 是否已認證

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "created")
    private LocalDateTime createTime;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<ProductVO> product;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<OrdVO>  order;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Integer getSellerNo() {
        return sellerNo;
    }

    public void setSellerNo(Integer sellerNo) {
        this.sellerNo = sellerNo;
    }

    public MembersVO getMember() {
        return member;
    }

    public void setMember(MembersVO member) {
        this.member = member;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(String sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
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