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

/**
 * 安裝服務前端控制器 (Installation Frontend Controller)
 * 負責處理使用者瀏覽技師、查看詳情、預約安裝、以及申請成為技師等功能。
 */
@Controller
@RequestMapping("/installation")
@RequiredArgsConstructor
public class InstallationController {

    private final TechnicianManagementService technicianService;
    private final BookingService bookingService;
    private final InstallLocationRepository locationRepository;
    private final TechnicianRepository technicianRepository;

    /**
     * 安裝服務首頁：技師列表與搜尋 (Installation Home: Technician List)
     */
    @GetMapping("/home")
    public String home(@RequestParam(required = false) Integer region,
            @RequestParam(required = false) List<Integer> serviceIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            HttpSession session,
            Model model) {

        // 1. 取得技師分頁數據 (包含篩選條件與排序)
        org.springframework.data.domain.Page<TechnicianCardVO> techPage = technicianService.getAllActiveTechnicians(
                region, serviceIds, keyword, page, size, sort);

        model.addAttribute("technicians", techPage.getContent());
        model.addAttribute("page", techPage);
        model.addAttribute("currentRegion", region);
        model.addAttribute("currentServiceIds", serviceIds);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentSort", sort);

        // 2. 檢查目前登入者身分 (是否已是技師、是否是管理員)
        MembersVO member = (MembersVO) session.getAttribute("member");
        boolean isTechnician = false;
        if (member != null) {
            isTechnician = technicianRepository.findByMemberMemNo(member.getMemberNo())
                    .map(t -> t.getIsActive() == 1) // 1 代表已核准且活躍
                    .orElse(false);
        }
        model.addAttribute("isTechnician", isTechnician);

        boolean isAdmin = session.getAttribute("admin") != null;
        model.addAttribute("isAdmin", isAdmin);

        // 3. 側邊欄：導航所需的服務項目數據
        model.addAttribute("serviceItems", technicianService.getTopServiceItems());
        model.addAttribute("allServiceItems", technicianService.getAllServiceItems());

        return "install/technician-list";
    }

    /**
     * 技師個人詳情頁 (Technician Detail Page)
     */
    @GetMapping("/technician/{id}")
    public String technicianDetail(@PathVariable Integer id, HttpSession session, Model model) {
        // 取得技師詳細資訊
        TechnicianCardVO technician = technicianService.getTechnicianDetail(id);
        model.addAttribute("technician", technician);

        // 取得預約時所需的安裝據點列表
        List<InstallLocation> locations = locationRepository.findAll();
        model.addAttribute("locations", locations);

        // 權限檢查：是否為技師
        MembersVO member = (MembersVO) session.getAttribute("member");
        boolean isTechnician = false;
        if (member != null) {
            isTechnician = technicianRepository.findByMemberMemNo(member.getMemberNo())
                    .map(t -> t.getIsActive() == 1)
                    .orElse(false);
        }
        model.addAttribute("isTechnician", isTechnician);

        return "install/technician-detail";
    }

    /**
     * AJAX 獲取可預約時段 (API for Availability)
     * 用於詳情頁切換據點或日期時，動態載入技師的可預約時間
     */
    @GetMapping("/api/availability")
    @ResponseBody
    public List<AvailableSlotVO> getAvailability(@RequestParam Integer techNo,
            @RequestParam Integer locationNo) {
        return bookingService.getAvailableSlots(techNo, locationNo);
    }

    /**
     * 處理預約表單提交 (Submit Booking)
     */
    @PostMapping("/book")
    public String createBooking(BookingRequestDTO dto, HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        // 設定下單會員編號並呼叫 Service 建立訂單
        dto.setMemberNo(member.getMemberNo());
        bookingService.createOrder(dto);

        // 導向會員訂單列表
        return "redirect:/member/orders";
    }

    /**
     * 技師申請頁面 (Technician Application Page)
     */
    @GetMapping("/apply-technician")
    public String applyPage(HttpSession session, Model model) {
        // 1. 確保已登入
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";
        model.addAttribute("member", member);

        // 2. 檢查是否曾申請過（若為被拒絕狀態，可取出舊資料帶入表單）
        technicianRepository.findByMemberMemNo(member.getMemberNo()).ifPresent(tech -> {
            model.addAttribute("existingTech", tech);
        });

        // 3. 確認目前是否已具備技師身分
        boolean isTechnician = technicianRepository.findByMemberMemNo(member.getMemberNo())
                .map(t -> t.getIsActive() == 1)
                .orElse(false);
        model.addAttribute("isTechnician", isTechnician);

        return "install/technician-apply";
    }

    /**
     * 處理技師申請表單提交 (Submit Application)
     */
    @PostMapping("/apply-technician")
    public String submitApplication(TechnicianOnboardingDTO dto,
            @RequestParam("profilePhoto") MultipartFile file,
            HttpSession session) throws IOException {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        // 封裝申請資料與照片文件
        dto.setMemberNo(member.getMemberNo());
        dto.setProfilePhoto(file);

        // 呼叫 Service 儲存申請 (預設為待審核狀態)
        technicianService.applyAsTechnician(dto);
        return "redirect:/installation/home";
    }

    /**
     * 技師後台功能進入點 (Technician Backend Entry)
     * 根據技師狀態（未申請、審核中、已核准、被拒絕）進行不同導向
     */
    @GetMapping("/entry")
    public String technicianEntry(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) {
            return "redirect:/members/login";
        }

        // 檢查技師身分狀態
        java.util.Optional<Technician> techOpt = technicianRepository.findByMemberMemNo(member.getMemberNo());
        if (techOpt.isPresent()) {
            Technician tech = techOpt.get();
            if (tech.getIsActive() == 1) {
                // 已核准：導向技師訂單管理
                return "redirect:/technician/orders";
            } else if (tech.getIsActive() == 2) {
                // 被拒絕：導向重寫申請頁面
                return "redirect:/installation/apply-technician?reapply=true";
            } else {
                // 等待審核中或停權中
                return "redirect:/installation/home?error=pending";
            }
        } else {
            // 未申請：導向申請頁面
            return "redirect:/installation/apply-technician";
        }
    }
}
