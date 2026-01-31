package com.karshop.orderProd;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "ord")
public class OrderProd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_no")
    private Integer ordNo;

    @NotNull
    @Column(name = "seller_no")
    private Integer sellerNo;

    @NotNull
    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "coupon_no")
    private Integer couponNo;

    @NotNull
    @Column(name = "ord_date")
    private LocalDateTime ordDate;

    @NotNull
    @Column(name = "origin_price")
    private Integer originPrice;

    @NotNull
    @Column(name = "discount_price")
    private Integer discountPrice;

    @NotNull
    @Column(name = "ord_price")
    private Integer ordPrice;

    @NotNull
    @Column(name = "ord_status", length = 5)
    private String ordStatus; // 待付款、待發貨、已發貨、已完成、已取消、未取貨

    @NotNull
    @Column(name = "ord_payment_status", length = 5)
    private String ordPaymentStatus; // 已支付、未支付、退款中

    @NotNull
    @Column(name = "ord_payment_method", length = 15)
    private String ordPaymentMethod; // 信用卡、LINE PAY、貨到付款

    @NotNull
    @Column(name = "ord_ship_method", length = 10)
    private String ordShipMethod; // 宅配、超取

    @Column(name = "ord_ship_no", length = 50)
    private String ordShipNo;

    @NotNull
    @Column(name = "ord_recipient", length = 10)
    private String ordRecipient;

    @NotNull
    @Column(name = "ord_address", length = 100)
    private String ordAddress;

    @Column(name = "ord_completed_date")
    private LocalDateTime ordCompletedDate;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "payout_status", length = 20)
    private String payoutStatus = "未撥款"; // 預設值

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "ord_no", referencedColumnName = "ord_no", insertable = false, updatable = false)
    private List<OrderProdDetail> ordDetails; // 變數名為 OrderProdDetail
}