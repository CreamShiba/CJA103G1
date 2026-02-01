package com.karshop.install.controller;

import com.karshop.install.entity.InstallOrder;
import com.karshop.members.model.MembersVO; // Updated import
import com.karshop.install.repository.InstallOrderRepository;
import com.karshop.install.service.PaymentService;
import com.karshop.install.vo.OrderVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberOrderController {
    // Force recompile check

    private final InstallOrderRepository orderRepository;
    private final PaymentService paymentService;
    private final com.karshop.install.service.ReviewService reviewService;
    private final com.karshop.install.repository.TechnicianRepository technicianRepository;

    @GetMapping("/orders")
    public String myOrders(@RequestParam(required = false) Integer status, HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        List<InstallOrder> orders;
        if (status != null) {
            orders = orderRepository.findByMemberMemIdAndOrderStatusValueOrderByInstallOrderNoDesc(member.getMemberNo(),
                    status);
        } else {
            orders = orderRepository.findByMemberMemIdOrderByInstallOrderNoDesc(member.getMemberNo());
        }

        boolean isTechnician = technicianRepository.findByMemberMemId(member.getMemberNo())
                .map(t -> t.getIsActive() == 1)
                .orElse(false);
        model.addAttribute("isTechnician", isTechnician);

        model.addAttribute("currentStatus", status);
        List<OrderVO> voList = orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setInstallOrderNo(o.getInstallOrderNo());
            vo.setTechnicianName(o.getTechnician().getRealName());
            vo.setLocationName(o.getLocation().getLocationName());
            vo.setAppointDate(o.getAppointDate());
            vo.setAppointTime(o.getAppointTime().getDescription());
            vo.setTotalPrice(o.getTotalPrice());
            vo.setOrderStatus(o.getOrderStatus().getDescription());
            vo.setPayStatus(o.getPayStatus().getDescription());

            // Check if reviewed
            if (o.getOrderStatus() == com.karshop.install.enums.OrderStatus.COMPLETED) {
                vo.setReviewed(reviewService.hasReviewed(o.getInstallOrderNo()));
            }

            return vo;
        }).collect(Collectors.toList());

        model.addAttribute("orders", voList);
        return "install/member-orders";
    }

    @GetMapping("/order/{id}/pay")
    public void goToPay(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        String formHtml = paymentService.generateECPayForm(id);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(formHtml);
        out.close();
    }

    @GetMapping("/order/{id}/review")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String reviewPage(@PathVariable Integer id, HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        InstallOrder order = orderRepository.findByIdWithDetails(id).orElse(null);
        if (order == null || !order.getMember().getMemberNo().equals(member.getMemberNo())) {
            return "redirect:/member/orders";
        }

        if (reviewService.hasReviewed(id)) {
            return "redirect:/member/orders"; // Already reviewed
        }

        model.addAttribute("order", order);
        return "install/review-form";
    }

    @PostMapping("/order/{id}/review")
    public String submitReview(@PathVariable Integer id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        reviewService.submitReview(id, rating, comment);
        return "redirect:/member/orders";
    }
}
