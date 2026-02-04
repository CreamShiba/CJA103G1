package com.karshop.ord.model;

import com.karshop.buyerrating.model.BuyerRatingVO;
import com.karshop.members.model.MembersVO;
import com.karshop.orddetail.model.OrdDetailVO;
import com.karshop.rating.model.RatingVO;
import com.karshop.sellertest.model.SellerVO;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_no")
    private SellerVO seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private MembersVO member;

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

//  賣家寫的評價
    @OneToOne(mappedBy = "ord")
    private RatingVO sellerRating;

    @Column(name = "payout_status")
    private String payoutStatus;

//  買家寫的評價
    @OneToMany(mappedBy = "ord", cascade =  CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BuyerRatingVO> buyerRatings;

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

    public RatingVO getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(RatingVO sellerRating) {
        this.sellerRating = sellerRating;
    }

    public String getPayoutStatus() {
        return payoutStatus;
    }

    public void setPayoutStatus(String payoutStatus) {
        this.payoutStatus = payoutStatus;
    }

    public List<BuyerRatingVO> getBuyerRatings() {
        return buyerRatings;
    }

    public void setBuyerRatings(List<BuyerRatingVO> buyerRatings) {
        this.buyerRatings = buyerRatings;
    }
}

