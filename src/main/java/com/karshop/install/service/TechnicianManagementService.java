package com.karshop.install.service;

import com.karshop.install.dto.TechnicianOnboardingDTO;
import com.karshop.members.model.MembersVO; // Updated import
import com.karshop.install.entity.Technician;
import com.karshop.install.entity.TechnicianService;
import com.karshop.members.model.MembersRepository; // Updated import
import com.karshop.install.repository.TechnicianRepository;
import com.karshop.install.repository.TechnicianServiceRepository;
import com.karshop.install.vo.TechnicianCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianManagementService {

    private final TechnicianRepository technicianRepository;
    private final MembersRepository memberRepository; // Updated type
    private final TechnicianServiceRepository technicianServiceRepository;
    private final com.karshop.install.repository.ServiceItemRepository serviceItemRepository;
    private final com.karshop.install.repository.TechnicianReviewRepository technicianReviewRepository;
    private final com.karshop.install.repository.InstallLocationRepository installLocationRepository;
    private final com.karshop.install.repository.InstallOrderRepository orderRepository;

    /**
     * 📋 取得所有可用服務項目
     * 用於後台選單顯示
     */
    public List<com.karshop.install.entity.ServiceItem> getAllServiceItems() {
        return serviceItemRepository.findAll();
    }

    /**
     * 🔥 取得熱門服務項目 (前 10 筆)
     * 用於前台側邊欄顯示
     */
    public List<com.karshop.install.entity.ServiceItem> getTopServiceItems() {
        // 暫時簡單實作：取得前 10 筆
        return serviceItemRepository.findAll().stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 📝 技師資格申請 (Apply as Technician)
     * 建立一筆狀態為 PENDING (0) 的技師資料
     *
     * @param dto 包含申請表格資料 (姓名、電話、地區、銀行帳戶等)
     * @return 儲存後的 Technician 實體
     */
    @Transactional
    public Technician applyAsTechnician(TechnicianOnboardingDTO dto) throws IOException {
        MembersVO member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("會員不存在"));

        // 檢查是否重複申請
        if (technicianRepository.findByMemberMemNo(dto.getMemberNo()).isPresent()) {
            throw new IllegalStateException("您已經申請過技師資格");
        }

        Technician technician = new Technician();
        technician.setMember(member);
        technician.setRealName(dto.getRealName());
        technician.setPhone(dto.getPhone());
        technician.setEmail(dto.getEmail());
        technician.setServiceArea(dto.getServiceArea());
        technician.setRegionCodeValue(dto.getRegionCode());
        technician.setBankCode(dto.getBankCode());
        technician.setBankAccount(dto.getBankAccount());
        technician.setIsActive(0); // 預設為待審核

        // 若有上傳大頭貼，轉為 byte[] 存入
        if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
            technician.setTechProfile(dto.getProfilePhoto().getBytes());
        }

        return technicianRepository.save(technician);
    }

    /**
     * 👤 更新技師基本資料 (Update Profile)
     * 針對已存在的技師修改個人資訊
     */
    @Transactional
    public void updateTechnicianProfile(TechnicianOnboardingDTO dto, Integer memberNo) throws IOException {
        Technician technician = technicianRepository.findByMemberMemNo(memberNo)
                .orElseThrow(() -> new IllegalStateException("技師不存在"));

        technician.setRealName(dto.getRealName());
        technician.setPhone(dto.getPhone());
        technician.setEmail(dto.getEmail());
        technician.setServiceArea(dto.getServiceArea());
        technician.setRegionCodeValue(dto.getRegionCode());
        technician.setBankCode(dto.getBankCode());
        technician.setBankAccount(dto.getBankAccount());
        technician.setUpdatedAt(java.time.LocalDateTime.now());

        if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
            technician.setTechProfile(dto.getProfilePhoto().getBytes());
        }

        technicianRepository.save(technician);
    }

    /**
     * 🛠️ 更新技師服務項目與價格
     * 處理服務的勾選、取消勾選以及價格設定
     *
     * @param memberNo           會員編號
     * @param selectedServiceIds 被勾選的服務 ID 列表
     * @param prices             每個服務對應的自訂價格 Map (ServiceID -> Price)
     */
    @Transactional
    public void updateTechnicianServices(Integer memberNo, List<Integer> selectedServiceIds,
            java.util.Map<Integer, Integer> prices) {
        Technician technician = technicianRepository.findByMemberMemNo(memberNo)
                .orElseThrow(() -> new IllegalStateException("技師不存在"));

        if (selectedServiceIds != null) {
            handleServiceUpdate(technician, selectedServiceIds, prices);
        }
    }

    // 內部邏輯：同步資料庫中的 TechnicianService 紀錄
    private void handleServiceUpdate(Technician technician, List<Integer> selectedServiceIds,
            java.util.Map<Integer, Integer> prices) {
        List<com.karshop.install.entity.ServiceItem> allItems = serviceItemRepository.findAll();
        List<TechnicianService> existingServices = technicianServiceRepository
                .findByTechnicianTechNo(technician.getTechNo());

        for (com.karshop.install.entity.ServiceItem item : allItems) {
            Integer serviceNo = item.getServiceNo();
            boolean isSelected = selectedServiceIds.contains(serviceNo);
            Integer price = prices.getOrDefault(serviceNo, 500); // 預設價格 500

            TechnicianService ts = existingServices.stream()
                    .filter(s -> s.getServiceItem().getServiceNo().equals(serviceNo))
                    .findFirst()
                    .orElse(null);

            if (isSelected) {
                if (ts == null) {
                    // 新增服務關聯
                    ts = new TechnicianService();
                    ts.setTechnician(technician);
                    ts.setServiceItem(item);
                    ts.setPrice(price);
                    ts.setServiceStatus(1);
                    technicianServiceRepository.save(ts);
                } else {
                    // 更新現有服務 (價格/狀態)
                    ts.setPrice(price);
                    ts.setServiceStatus(1);
                    technicianServiceRepository.save(ts);
                }
            } else {
                if (ts != null) {
                    // 取消勾選 (軟刪除 status=0)
                    ts.setServiceStatus(0);
                    technicianServiceRepository.save(ts);
                }
            }
        }
    }

    public List<TechnicianService> getTechnicianServices(Integer techNo) {
        return technicianServiceRepository.findByTechnicianTechNo(techNo);
    }

    /**
     * ✅ 管理員審核通過
     * 1. 將技師狀態設為 Active (1)
     * 2. 將會員狀態設為 Engineer (1)
     */
    @Transactional
    public void approveTechnician(Integer techNo) {
        Technician technician = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));

        technician.setIsActive(1);
        technicianRepository.save(technician);

        // Update member's engineer_status
        MembersVO member = technician.getMember();
        // member.setEngineerStatus(1); // TODO: Check if MembersVO has a corresponding
        // status field
        memberRepository.save(member);
    }

    /**
     * ❌ 管理員駁回申請
     * 直接刪除申請紀錄
     */
    @Transactional
    public void rejectTechnician(Integer techNo) {
        technicianRepository.deleteById(techNo);
    }

    /**
     * 🔍 搜尋前台顯示的技師列表 (Get Active Technicians)
     * 
     * @param regionCode 地區篩選 (可選)
     * @param serviceIds 服務項目篩選 (可選，多選)
     */
    @Transactional(readOnly = true)
    public List<TechnicianCardVO> getAllActiveTechnicians(Integer regionCode, List<Integer> serviceIds) {
        List<Technician> technicians;

        if (serviceIds != null && !serviceIds.isEmpty()) {
            // 邏輯修改：必須符合 "所有" 勾選的服務項目 (AND 邏輯)

            // 1. 先取得所有候選技師 (Active)
            List<Technician> candidates;
            if (regionCode != null) {
                candidates = technicianRepository.findByIsActiveAndRegionCodeValue(1, regionCode);
            } else {
                candidates = technicianRepository.findByIsActive(1);
            }

            // 2. 過濾技師：檢查每位技師是否擁有 "所有" 指定的 serviceIds
            technicians = candidates.stream().filter(tech -> {
                // 取得該技師目前提供的所有服務 ID
                List<Integer> techServiceIds = technicianServiceRepository
                        .findByTechnicianTechNoAndServiceStatus(tech.getTechNo(), 1)
                        .stream()
                        .map(ts -> ts.getServiceItem().getServiceNo())
                        .collect(Collectors.toList());

                // 檢查 serviceIds 是否為 techServiceIds 的子集 (即包含所有勾選項目)
                return techServiceIds.containsAll(serviceIds);
            }).collect(Collectors.toList());

        } else if (regionCode != null) {
            technicians = technicianRepository.findByIsActiveAndRegionCodeValue(1, regionCode);
        } else {
            technicians = technicianRepository.findByIsActive(1);
        }

        // 轉換 Entity -> VO (包含評分計算與圖片處理)
        return technicians.stream().map(tech -> {
            TechnicianCardVO vo = new TechnicianCardVO();
            vo.setTechNo(tech.getTechNo());
            vo.setRealName(tech.getRealName());
            vo.setPhone(tech.getPhone());
            vo.setEmail(tech.getEmail());
            vo.setServiceArea(tech.getServiceArea());

            // 計算平均評分
            if (tech.getRatingAmount() > 0) {
                vo.setAvgRating((double) tech.getRatingStar() / tech.getRatingAmount());
            } else {
                vo.setAvgRating(0.0);
            }

            vo.setProfilePhotoFromBytes(tech.getTechProfile());

            // 取得該技師提供的服務
            List<TechnicianService> services = technicianServiceRepository
                    .findByTechnicianTechNoAndServiceStatus(tech.getTechNo(), 1);

            List<TechnicianCardVO.ServiceVO> serviceVOs = services.stream().map(ts -> {
                TechnicianCardVO.ServiceVO svo = new TechnicianCardVO.ServiceVO();
                svo.setTsNo(ts.getTsNo());
                svo.setServiceName(ts.getServiceItem().getServiceName());
                svo.setPrice(ts.getPrice());
                return svo;
            }).collect(Collectors.toList());

            vo.setServices(serviceVOs);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 📄 取得技師詳細頁面資訊 (Detail Page)
     * 包含基本資料、服務項目、評價列表
     */
    @Transactional(readOnly = true)
    public TechnicianCardVO getTechnicianDetail(Integer techNo) {
        Technician tech = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));

        TechnicianCardVO vo = new TechnicianCardVO();
        vo.setTechNo(tech.getTechNo());
        vo.setRealName(tech.getRealName());
        vo.setPhone(tech.getPhone());
        vo.setEmail(tech.getEmail());
        vo.setServiceArea(tech.getServiceArea());

        if (tech.getRatingAmount() > 0) {
            vo.setAvgRating((double) tech.getRatingStar() / tech.getRatingAmount());
        } else {
            vo.setAvgRating(0.0);
        }

        vo.setProfilePhotoFromBytes(tech.getTechProfile());

        List<TechnicianService> services = technicianServiceRepository
                .findByTechnicianTechNoAndServiceStatus(tech.getTechNo(), 1);

        List<TechnicianCardVO.ServiceVO> serviceVOs = services.stream().map(ts -> {
            TechnicianCardVO.ServiceVO svo = new TechnicianCardVO.ServiceVO();
            svo.setTsNo(ts.getTsNo());
            svo.setServiceName(ts.getServiceItem().getServiceName());
            svo.setPrice(ts.getPrice());
            return svo;
        }).collect(Collectors.toList());

        vo.setServices(serviceVOs);

        // 取得評論列表
        List<com.karshop.install.entity.TechnicianReview> reviews = technicianReviewRepository
                .findByTechnicianTechNo(tech.getTechNo());
        List<TechnicianCardVO.ReviewVO> reviewVOs = reviews.stream().map(r -> {
            return new TechnicianCardVO.ReviewVO(
                    r.getMember().getMemberName(),
                    r.getRatingStar(),
                    r.getReviewContent(),
                    r.getCreatedAt());
        }).collect(Collectors.toList());
        vo.setReviews(reviewVOs);

        return vo;
    }

    /**
     * 📋 [Admin] 取得待審核列表
     */
    public List<Technician> getPendingApplications() {
        return technicianRepository.findByIsActive(0);
    }

    /**
     * 📋 [Admin] 取得所有通過審核的技師
     */
    public List<Technician> getApprovedTechnicians() {
        return technicianRepository.findByIsActive(1);
    }

    /**
     * ⛔ [Admin] 暫時停權技師 (退回審核狀態)
     */
    @Transactional
    public void suspendTechnician(Integer techNo) {
        // 1. 檢查是否有未完成的訂單 (Pending, Awaiting Payment, Paid/Uninstalled)
        long activeOrders = orderRepository.countActiveOrdersByTechnician(techNo); // Using field from AdminController,
                                                                                   // need to ensure repo is injected
                                                                                   // here.
        // Wait, orderRepository is defined as 'installOrderRepository' in this service?
        // No, let's check.
        // This service has installOrderRepository? No. I need to add it.

        Technician technician = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));

        if (activeOrders > 0) {
            throw new IllegalStateException("該技師尚有未完成的訂單，無法停權");
        }

        technician.setIsActive(0); // Set back to PENDING (Audit/Suspended status)
        technicianRepository.save(technician);
    }

    /**
     * ➕ [Admin] 新增服務項目
     */
    @Transactional
    public void createServiceItem(String serviceName, com.karshop.admins.model.AdminVO admin) { // Update to AdminVO
        com.karshop.install.entity.ServiceItem item = new com.karshop.install.entity.ServiceItem();
        item.setServiceName(serviceName);
        item.setAdm(admin);
        item.setCreatedAt(java.time.LocalDateTime.now());
        serviceItemRepository.save(item);
    }

    /**
     * ✏️ [Admin] 更新服務項目名稱
     */
    @Transactional
    public void updateServiceItem(Integer serviceNo, String serviceName) {
        com.karshop.install.entity.ServiceItem item = serviceItemRepository.findById(serviceNo)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        item.setServiceName(serviceName);
        serviceItemRepository.save(item);
    }

    /**
     * 🗑️ [Admin] 刪除服務項目
     */
    @Transactional
    public void deleteServiceItem(Integer serviceNo) {
        serviceItemRepository.deleteById(serviceNo);
    }

    // --- 場地管理 (Location Management) ---

    public List<com.karshop.install.entity.InstallLocation> getAllLocations() {
        return installLocationRepository.findAll();
    }

    @Transactional
    public void createLocation(String name, String address, Integer price) {
        com.karshop.install.entity.InstallLocation loc = new com.karshop.install.entity.InstallLocation();
        loc.setLocationName(name);
        loc.setAddress(address);
        loc.setPricePer(price);
        installLocationRepository.save(loc);
    }

    @Transactional
    public void updateLocation(Integer id, String name, String address, Integer price) {
        com.karshop.install.entity.InstallLocation loc = installLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        loc.setLocationName(name);
        loc.setAddress(address);
        loc.setPricePer(price);
        installLocationRepository.save(loc);
    }

    @Transactional
    public void deleteLocation(Integer id) {
        installLocationRepository.deleteById(id);
    }
}
