package com.karshop.install.controller;

import com.karshop.install.dto.BookingRequestDTO;
import com.karshop.install.dto.TechnicianOnboardingDTO;
import com.karshop.install.entity.InstallLocation;
import com.karshop.install.entity.Technician;
import com.karshop.members.model.MembersVO;
import com.karshop.install.repository.InstallLocationRepository;
import com.karshop.install.repository.TechnicianRepository;
import com.karshop.install.service.BookingService;
import com.karshop.install.service.TechnicianManagementService;
import com.karshop.install.vo.AvailableSlotVO;
import com.karshop.install.vo.TechnicianCardVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/installation")
@RequiredArgsConstructor
public class InstallationController {

    private final TechnicianManagementService technicianService;
    private final BookingService bookingService;
    private final InstallLocationRepository locationRepository;
    private final TechnicianRepository technicianRepository;

    @GetMapping("/home")
    public String home(@RequestParam(required = false) Integer region,
            @RequestParam(required = false) List<Integer> serviceIds,
            HttpSession session,
            Model model) {
        List<TechnicianCardVO> technicians = technicianService.getAllActiveTechnicians(region, serviceIds);
        model.addAttribute("technicians", technicians);
        model.addAttribute("currentRegion", region);
        model.addAttribute("currentServiceIds", serviceIds);

        MembersVO member = (MembersVO) session.getAttribute("member");
        boolean isTechnician = false;
        if (member != null) {
            isTechnician = technicianRepository.findByMemberMemId(member.getMemberNo())
                    .map(t -> t.getIsActive() == 1) // Assuming 1 means active/approved
                    .orElse(false);
        }
        model.addAttribute("isTechnician", isTechnician);

        // Check if user is an admin
        boolean isAdmin = session.getAttribute("admin") != null;
        model.addAttribute("isAdmin", isAdmin);

        // Sidebar: Top Services
        model.addAttribute("serviceItems", technicianService.getTopServiceItems());
        // Sidebar: All Services for Filter
        model.addAttribute("allServiceItems", technicianService.getAllServiceItems());

        return "install/technician-list";
    }

    @GetMapping("/technician/{id}")
    public String technicianDetail(@PathVariable Integer id, Model model) {
        TechnicianCardVO technician = technicianService.getTechnicianDetail(id);
        model.addAttribute("technician", technician);

        List<InstallLocation> locations = locationRepository.findAll();
        model.addAttribute("locations", locations);

        return "install/technician-detail";
    }

    // AJAX for availability
    @GetMapping("/api/availability")
    @ResponseBody
    public List<AvailableSlotVO> getAvailability(@RequestParam Integer techNo,
            @RequestParam Integer locationNo) {
        return bookingService.getAvailableSlots(techNo, locationNo);
    }

    @PostMapping("/book")
    public String createBooking(BookingRequestDTO dto, HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        dto.setMemberNo(member.getMemberNo());
        bookingService.createOrder(dto);

        return "redirect:/member/orders";
    }

    @GetMapping("/apply-technician")
    public String applyPage(HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";
        model.addAttribute("member", member);
        return "install/technician-apply";
    }

    @PostMapping("/apply-technician")
    public String submitApplication(TechnicianOnboardingDTO dto,
            @RequestParam("profilePhoto") MultipartFile file,
            HttpSession session) throws IOException {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        dto.setMemberNo(member.getMemberNo());
        dto.setProfilePhoto(file);

        technicianService.applyAsTechnician(dto);
        return "redirect:/installation/home";
    }

    @GetMapping("/entry")
    public String technicianEntry(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) {
            return "redirect:/members/login";
        }

        // Check if user is already a technician
        java.util.Optional<Technician> techOpt = technicianRepository.findByMemberMemId(member.getMemberNo());
        if (techOpt.isPresent()) {
            Technician tech = techOpt.get();
            if (tech.getIsActive() == 1) {
                return "redirect:/technician/orders";
            } else {
                // Pending approval or suspended
                return "redirect:/installation/home?error=pending";
            }
        } else {
            return "redirect:/installation/apply-technician";
        }
    }
}
