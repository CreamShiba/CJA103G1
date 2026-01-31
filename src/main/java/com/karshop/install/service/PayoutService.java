package com.karshop.install.service;

import com.karshop.install.entity.InstallOrder;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.enums.PayoutStatus;
import com.karshop.install.repository.InstallOrderRepository;
import com.karshop.install.vo.PayoutVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final InstallOrderRepository orderRepository;

    /**
     * Complete Order & Calculate Payout
     * Formula:
     * Platform Revenue = Venue Fee + (Tech Service Fee * 5%)
     * Tech Payout = Tech Service Fee * 95%
     */
    @Transactional
    public void completeOrder(Integer orderNo, Integer techNo) {
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!order.getTechnician().getTechNo().equals(techNo)) {
            throw new IllegalArgumentException("無權限");
        }

        if (order.getOrderStatus() != OrderStatus.PAID_UNINSTALLED) {
            throw new IllegalStateException("訂單狀態錯誤，無法完工");
        }

        // Calculate Payouts
        int totalPrice = order.getTotalPrice();
        int venueFee = order.getVenueFee();
        int serviceFee = totalPrice - venueFee;

        // Rules:
        // Platform gets Venue Fee + 5% of Service Fee
        // Tech gets 95% of Service Fee

        int platformCutFromService = (int) Math.round(serviceFee * 0.05);
        int techCutFromService = (int) Math.round(serviceFee * 0.95);

        // Handle rounding difference if any (e.g. 100 -> 5+95 ok. 33 -> 1.65+31.35 ->
        // 2+31=33)
        // Adjust tech payout to ensure sum matches serviceFee
        if (platformCutFromService + techCutFromService != serviceFee) {
            techCutFromService = serviceFee - platformCutFromService;
        }

        order.setPlatformRevenue(venueFee + platformCutFromService);
        order.setTechPayout(techCutFromService);

        order.setOrderStatus(OrderStatus.COMPLETED);
        // Payout status remains PENDING until Admin executes specific payout action?
        // Or if this "Complete" action is just marking work done.

        orderRepository.save(order);
    }

    /**
     * Get orders for Admin Payout view
     */
    public List<PayoutVO> getCompletedOrdersForPayout() {
        // Find orders that are COMPLETED but Payout is PENDING?
        // Or just all COMPLETED orders.
        // Let's assume we want to show all completed orders.
        List<InstallOrder> orders = orderRepository.findByOrderStatusValue(OrderStatus.COMPLETED.getCode());

        return orders.stream().map(o -> {
            PayoutVO vo = new PayoutVO();
            vo.setInstallOrderNo(o.getInstallOrderNo());
            vo.setTechnicianName(o.getTechnician().getRealName());
            vo.setBankAccount(o.getTechnician().getBankCode() + "-" + o.getTechnician().getBankAccount());
            vo.setTotalPrice(o.getTotalPrice());
            vo.setVenueFee(o.getVenueFee());
            vo.setServiceFee(o.getTotalPrice() - o.getVenueFee());
            vo.setPlatformRevenue(o.getPlatformRevenue());
            vo.setTechPayout(o.getTechPayout());
            vo.setPayoutStatus(o.getPayoutStatus().getDescription());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * Execute Payout (Admin Action)
     */
    @Transactional
    public void executePayout(Integer orderNo) {
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (order.getPayoutStatus() == PayoutStatus.COMPLETED) {
            throw new IllegalStateException("已撥款");
        }

        order.setPayoutStatus(PayoutStatus.COMPLETED);
        orderRepository.save(order);
        // In real world, call bank API transfer here
    }
}
