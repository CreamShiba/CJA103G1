package com.karshop.install.controller;

import com.karshop.install.entity.InstallOrder;
import com.karshop.members.model.MembersVO; // Updated import
import com.karshop.install.entity.Technician;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.repository.InstallOrderRepository;
import com.karshop.install.repository.TechnicianRepository;
import com.karshop.install.service.BookingService;
import com.karshop.install.service.PayoutService;
import com.karshop.install.vo.OrderVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
@RequiredArgsConstructor
public class TechnicianBackendController {

        private final InstallOrderRepository orderRepository;
        private final TechnicianRepository technicianRepository;
        private final BookingService bookingService;
        private final PayoutService payoutService;
        private final com.karshop.install.service.TechnicianManagementService technicianService;

        @GetMapping("/orders")
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public String technicianOrders(@RequestParam(required = false, defaultValue = "pending") String tab,
                        HttpSession session, Model model) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                if (member == null)
                        return "redirect:/members/login";

                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));

                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                model.addAttribute("currentTab", tab);

                // 1. Pending Orders (待確認)
                List<InstallOrder> pendingOrders = orderRepository.findByTechnicianTechNoAndOrderStatusValue(
                                tech.getTechNo(), OrderStatus.PENDING.getCode());
                model.addAttribute("pendingOrders",
                                pendingOrders.stream().map(this::convertToVO).collect(Collectors.toList()));

                // 2. Accepted but Unpaid (已接受 / 待付款)
                List<InstallOrder> acceptedOrders = orderRepository.findByTechnicianTechNoAndOrderStatusValue(
                                tech.getTechNo(), OrderStatus.AWAITING_PAYMENT.getCode());
                model.addAttribute("acceptedOrders",
                                acceptedOrders.stream().map(this::convertToVO).collect(Collectors.toList()));

                // 3. Paid & Active (進行中 / 已付款待安裝)
                List<InstallOrder> paidOrders = orderRepository.findByTechnicianTechNoAndOrderStatusValue(
                                tech.getTechNo(), OrderStatus.PAID_UNINSTALLED.getCode());
                model.addAttribute("paidOrders",
                                paidOrders.stream().map(this::convertToVO).collect(Collectors.toList()));

                // 5. Completed History (All Completed Orders)
                List<InstallOrder> completedOrders = orderRepository.findByTechnicianTechNoAndOrderStatusValue(
                                tech.getTechNo(), OrderStatus.COMPLETED.getCode());
                model.addAttribute("completedOrders",
                                completedOrders.stream().map(this::convertToVO).collect(Collectors.toList()));

                return "install/technician-backend";
        }

        @PostMapping("/order/{id}/accept")
        public String acceptOrder(@PathVariable Integer id, HttpSession session) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));
                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                bookingService.confirmOrder(id, tech.getTechNo());
                return "redirect:/technician/orders";
        }

        @PostMapping("/order/{id}/reject")
        public String rejectOrder(@PathVariable Integer id, HttpSession session) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));
                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                bookingService.rejectOrder(id, tech.getTechNo());
                return "redirect:/technician/orders";
        }

        @PostMapping("/order/{id}/complete")
        public String completeOrder(@PathVariable Integer id, HttpSession session) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));
                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                payoutService.completeOrder(id, tech.getTechNo());
                return "redirect:/technician/orders";
        }

        private OrderVO convertToVO(InstallOrder o) {
                OrderVO vo = new OrderVO();
                vo.setInstallOrderNo(o.getInstallOrderNo());
                vo.setMemberName(o.getMember().getMemberName()); // Add this field to VO if needed
                vo.setLocationName(o.getLocation().getLocationName());
                vo.setAppointDate(o.getAppointDate());
                vo.setAppointTime(o.getAppointTime().getDescription());
                vo.setTotalPrice(o.getTotalPrice());
                vo.setOrderStatus(o.getOrderStatus().getDescription());
                vo.setPayoutStatus(o.getPayoutStatus().getDescription());
                return vo;
        }

        @GetMapping("/edit")
        public String editProfilePage(HttpSession session, Model model) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                if (member == null)
                        return "redirect:/members/login";

                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));
                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                // Populate DTO for form
                com.karshop.install.dto.TechnicianOnboardingDTO dto = new com.karshop.install.dto.TechnicianOnboardingDTO();
                dto.setRealName(tech.getRealName());
                dto.setPhone(tech.getPhone());
                dto.setEmail(tech.getEmail());
                dto.setServiceArea(tech.getServiceArea());
                dto.setRegionCode(tech.getRegionCodeValue());
                dto.setBankCode(tech.getBankCode());
                dto.setBankAccount(tech.getBankAccount());

                model.addAttribute("dto", dto);
                return "install/technician-edit";
        }

        @PostMapping("/edit")
        public String updateProfile(@ModelAttribute com.karshop.install.dto.TechnicianOnboardingDTO dto,
                        @RequestParam(value = "photoFile", required = false) org.springframework.web.multipart.MultipartFile file,
                        HttpSession session) throws java.io.IOException {
                MembersVO member = (MembersVO) session.getAttribute("member");
                if (member == null)
                        return "redirect:/members/login";

                // Explicitly set file if present
                if (file != null && !file.isEmpty()) {
                        dto.setProfilePhoto(file);
                }

                technicianService.updateTechnicianProfile(dto, member.getMemberNo());

                return "redirect:/technician/orders";
        }

        @GetMapping("/services")
        public String serviceManagementPage(HttpSession session, Model model) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                if (member == null)
                        return "redirect:/members/login";

                Technician tech = technicianRepository.findByMemberMemId(member.getMemberNo())
                                .orElseThrow(() -> new IllegalStateException("非技師身份"));
                if (tech.getIsActive() != 1)
                        return "redirect:/installation/home?error=pending";

                com.karshop.install.dto.TechnicianServicesDTO dto = new com.karshop.install.dto.TechnicianServicesDTO();

                // Populate Services
                List<com.karshop.install.entity.TechnicianService> myServices = technicianService
                                .getTechnicianServices(tech.getTechNo());
                for (com.karshop.install.entity.TechnicianService ts : myServices) {
                        if (ts.getServiceStatus() == 1) {
                                dto.getServiceIds().add(ts.getServiceItem().getServiceNo());
                                dto.getServicePrices().put(ts.getServiceItem().getServiceNo(), ts.getPrice());
                        }
                }

                model.addAttribute("allServiceItems", technicianService.getAllServiceItems());
                model.addAttribute("dto", dto);

                return "install/technician-services";
        }

        @PostMapping("/services")
        public String updateServices(com.karshop.install.dto.TechnicianServicesDTO dto, HttpSession session) {
                MembersVO member = (MembersVO) session.getAttribute("member");
                if (member == null)
                        return "redirect:/members/login";

                technicianService.updateTechnicianServices(member.getMemberNo(), dto.getServiceIds(),
                                dto.getServicePrices());

                return "redirect:/technician/orders";
        }
}
