package com.karshop.install.entity;

import com.karshop.install.enums.OrderStatus;
import com.karshop.install.enums.PayStatus;
import com.karshop.install.enums.PayoutStatus;
import com.karshop.install.enums.TimeSlot;
import com.karshop.members.model.MembersVO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "install_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstallOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "install_order_no")
    private Integer installOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private MembersVO member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_no", nullable = false)
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_no", nullable = false)
    private InstallLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_no")
    private LocationSchedule schedule;

    @Column(name = "appoint_date", nullable = false)
    private LocalDate appointDate;

    @Column(name = "appoint_time", nullable = false)
    private Integer appointTimeValue;

    @Column(name = "venue_fee", nullable = false)
    private Integer venueFee;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "platform_revenue")
    private Integer platformRevenue = 0;

    @Column(name = "tech_payout")
    private Integer techPayout = 0;

    @Column(name = "pay_status", nullable = false)
    private Integer payStatusValue = 0;

    @Column(name = "order_status", nullable = false)
    private Integer orderStatusValue = 0;

    @Column(name = "payout_status", nullable = false)
    private Integer payoutStatusValue = 0;

    @Column(name = "ecpay_trade_no", length = 50)
    private String ecpayTradeNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Transient fields for enum conversion
    @Transient
    public TimeSlot getAppointTime() {
        return TimeSlot.fromCode(appointTimeValue);
    }

    @Transient
    public void setAppointTime(TimeSlot timeSlot) {
        this.appointTimeValue = timeSlot.getCode();
    }

    @Transient
    public PayStatus getPayStatus() {
        return PayStatus.fromCode(payStatusValue);
    }

    @Transient
    public void setPayStatus(PayStatus payStatus) {
        this.payStatusValue = payStatus.getCode();
    }

    @Transient
    public OrderStatus getOrderStatus() {
        return OrderStatus.fromCode(orderStatusValue);
    }

    @Transient
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatusValue = orderStatus.getCode();
    }

    @Transient
    public PayoutStatus getPayoutStatus() {
        return PayoutStatus.fromCode(payoutStatusValue);
    }

    @Transient
    public void setPayoutStatus(PayoutStatus payoutStatus) {
        this.payoutStatusValue = payoutStatus.getCode();
    }
}
