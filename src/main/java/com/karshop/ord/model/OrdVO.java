package com.karshop.ord.model;

import com.karshop.orddetail.model.OrdDetailVO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ord")
public class OrdVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_no")
    private Integer ordNo;

    @Column(name = "seller_no")
    private Integer sellerNo;

    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "coupon_no")
    private Integer couponNo;

    @Column(name = "ord_date")
    private LocalDateTime ordDate;

    @Column(name = "origin_price")
    private Integer originPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "ord_price")
    private Integer ordPrice;

    @Column(name = "ord_status")
    private String ordStatus;

    @Column(name = "ord_payment_status")
    private String ordPaymentStatus;

    @Column(name = "ord_payment_method")
    private String ordPaymentMethod;

    @Column(name = "ord_ship_method")
    private String ordShipMethod;

    @Column(name = "ord_ship_no")
    private String ordShipNo;

    @Column(name = "ord_recipient")
    private String ordRecipient;

    @Column(name = "ord_address")
    private String ordAddress;

    @Column(name = "ord_completed_date")
    private LocalDateTime ordCompletedDate;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @OneToMany(mappedBy ="order", cascade = CascadeType.ALL)
    private List<OrdDetailVO> orderDetail;

//  平台抽成5%
    private static final double commission = 0.05;
//  平台手續費
    public Integer getPlatformFee(){
        return (int)Math.round(this.ordPrice*commission);
    }
//  賣家實收金額
    public Integer getSellerNetIncome(){
        return this.ordPrice - getPlatformFee();
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

    public Integer getCouponNo() {
        return couponNo;
    }

    public void setCouponNo(Integer couponNo) {
        this.couponNo = couponNo;
    }

    public LocalDateTime getOrdDate() {
        return ordDate;
    }

    public void setOrdDate(LocalDateTime ordDate) {
        this.ordDate = ordDate;
    }

    public Integer getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(Integer originPrice) {
        this.originPrice = originPrice;
    }

    public Integer getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(Integer discountPrice) {
        this.discountPrice = discountPrice;
    }

    public Integer getOrdPrice() {
        return ordPrice;
    }

    public void setOrdPrice(Integer ordPrice) {
        this.ordPrice = ordPrice;
    }

    public String getOrdStatus() {
        return ordStatus;
    }

    public void setOrdStatus(String ordStatus) {
        this.ordStatus = ordStatus;
    }

    public String getOrdPaymentStatus() {
        return ordPaymentStatus;
    }

    public void setOrdPaymentStatus(String ordPaymentStatus) {
        this.ordPaymentStatus = ordPaymentStatus;
    }

    public String getOrdPaymentMethod() {
        return ordPaymentMethod;
    }

    public void setOrdPaymentMethod(String ordPaymentMethod) {
        this.ordPaymentMethod = ordPaymentMethod;
    }

    public String getOrdShipMethod() {
        return ordShipMethod;
    }

    public void setOrdShipMethod(String ordShipMethod) {
        this.ordShipMethod = ordShipMethod;
    }

    public String getOrdShipNo() {
        return ordShipNo;
    }

    public void setOrdShipNo(String ordShipNo) {
        this.ordShipNo = ordShipNo;
    }

    public String getOrdRecipient() {
        return ordRecipient;
    }

    public void setOrdRecipient(String ordRecipient) {
        this.ordRecipient = ordRecipient;
    }

    public String getOrdAddress() {
        return ordAddress;
    }

    public void setOrdAddress(String ordAddress) {
        this.ordAddress = ordAddress;
    }

    public LocalDateTime getOrdCompletedDate() {
        return ordCompletedDate;
    }

    public void setOrdCompletedDate(LocalDateTime ordCompletedDate) {
        this.ordCompletedDate = ordCompletedDate;
    }

    public List<OrdDetailVO> getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(List<OrdDetailVO> orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}

