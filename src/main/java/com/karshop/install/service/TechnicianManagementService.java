package com.karshop.install.service;

import com.karshop.install.dto.TechnicianOnboardingDTO;
import com.karshop.members.model.MembersVO;
import com.karshop.install.entity.Technician;
import com.karshop.install.entity.TechnicianService;
import com.karshop.members.model.MembersRepository;
import com.karshop.install.repository.TechnicianRepository;
import com.karshop.install.repository.TechnicianServiceRepository;
import com.karshop.install.vo.TechnicianCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 技師管理服務 (Technician Management Service)
 * 負責處理技師的資格申請、資料修改、服務項目設定、以及管理員審核與據點維護。
 */
@Service
@RequiredArgsConstructor
public class TechnicianManagementService {

    private final TechnicianRepository technicianRepository;
    private final MembersRepository memberRepository;
    private final TechnicianServiceRepository technicianServiceRepository;
    private final com.karshop.install.repository.ServiceItemRepository serviceItemRepository;
    private final com.karshop.install.repository.TechnicianReviewRepository technicianReviewRepository;
    private final com.karshop.install.repository.InstallLocationRepository installLocationRepository;
    private final com.karshop.install.repository.InstallOrderRepository orderRepository;

    /**
     * 取得系統定義的所有基礎服務項目 (e.g. 輪胎更換, 機油保養)
     */
    public List<com.karshop.install.entity.ServiceItem> getAllServiceItems() {
        return serviceItemRepository.findAll();
    }

    /**
     * 取得熱門服務項目 (用於前台側邊欄推薦)
     */
    public List<com.karshop.install.entity.ServiceItem> getTopServiceItems() {
        return serviceItemRepository.findAll().stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 處理技師資格申請
     * 支持「新申請」與「遭駁回後重新申請」兩種流程。
     * 
     * @param dto 申請資料載體
     * @return 儲存後的技師實體
     */
    @Transactional
    public Technician applyAsTechnician(TechnicianOnboardingDTO dto) throws IOException {
        MembersVO member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("會員不存在"));

        // 1. 檢查是否已有申請紀錄
        java.util.Optional<Technician> existingTech = technicianRepository.findByMemberMemNo(dto.getMemberNo());
        if (existingTech.isPresent()) {
            Technician existing = existingTech.get();
            // 若狀態為「被拒絕 (2)」，則更新舊有紀錄並重設為「待審核 (0)」
            if (existing.getIsActive() == 2) {
                existing.setRealName(dto.getRealName());
                existing.setPhone(dto.getPhone());
                existing.setEmail(dto.getEmail());
                existing.setServiceArea(dto.getServiceArea());
                existing.setRegionCodeValue(dto.getRegionCode());
                existing.setBankCode(dto.getBankCode());
                existing.setBankAccount(dto.getBankAccount());
                existing.setIsActive(0);

                // 處理大頭貼：若沒傳新的且舊的也是空的，則給予預設圖
                if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
                    existing.setTechProfile(dto.getProfilePhoto().getBytes());
                } else if (existing.getTechProfile() == null || existing.getTechProfile().length == 0) {
                    existing.setTechProfile(loadDefaultProfileImage());
                }

                return technicianRepository.save(existing);
            } else {
                throw new IllegalStateException("您已經申請過技師資格，或正在審核中");
            }
        }

        // 2. 建立新技師紀錄
        Technician technician = new Technician();
        technician.setMember(member);
        technician.setRealName(dto.getRealName());
        technician.setPhone(dto.getPhone());
        technician.setEmail(dto.getEmail());
        technician.setServiceArea(dto.getServiceArea());
        technician.setRegionCodeValue(dto.getRegionCode());
        technician.setBankCode(dto.getBankCode());
        technician.setBankAccount(dto.getBankAccount());
        technician.setIsActive(0); // 初始狀態：待審核

        if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
            technician.setTechProfile(dto.getProfilePhoto().getBytes());
        } else {
            technician.setTechProfile(loadDefaultProfileImage());
        }

        return technicianRepository.save(technician);
    }

    private byte[] cachedDefaultProfileImage;

    /**
     * 載入預設的技師圖片檔
     */
    private byte[] loadDefaultProfileImage() {
        if (cachedDefaultProfileImage != null) {
            return cachedDefaultProfileImage;
        }
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(
                    "static/images/user.png");
            if (resource.exists()) {
                cachedDefaultProfileImage = resource.getContentAsByteArray();
                return cachedDefaultProfileImage;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 更新技師個人基本資料
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
     * 更新技師所提供的服務項目及其報價
     * 
     * @param memberNo           會員編號
     * @param selectedServiceIds 選中的基礎服務 ID
     * @param prices             技師針對每個服務設定的價格
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

    /**
     * 核心過濾與同步邏輯：管理技師服務項目的增刪。
     * 若原本勾選但後來取消，則實施「軟刪除」 (status=0) 以保留歷史訂單關聯。
     */
    private void handleServiceUpdate(Technician technician, List<Integer> selectedServiceIds,
            java.util.Map<Integer, Integer> prices) {
        List<com.karshop.install.entity.ServiceItem> allItems = serviceItemRepository.findAll();
        List<TechnicianService> existingServices = technicianServiceRepository
                .findByTechnicianTechNo(technician.getTechNo());

        for (com.karshop.install.entity.ServiceItem item : allItems) {
            Integer serviceNo = item.getServiceNo();
            boolean isSelected = selectedServiceIds.contains(serviceNo);
            Integer price = prices.getOrDefault(serviceNo, 500);

            // 檢查庫存中是否已存在此關聯
            TechnicianService ts = existingServices.stream()
                    .filter(s -> s.getServiceItem().getServiceNo().equals(serviceNo))
                    .findFirst()
                    .orElse(null);

            if (isSelected) {
                if (ts == null) {
                    // 新增技師個人的服務報價紀錄
                    ts = new TechnicianService();
                    ts.setTechnician(technician);
                    ts.setServiceItem(item);
                    ts.setPrice(price);
                    ts.setServiceStatus(1);
                    technicianServiceRepository.save(ts);
                } else {
                    // 更新報價與狀態
                    ts.setPrice(price);
                    ts.setServiceStatus(1);
                    technicianServiceRepository.save(ts);
                }
            } else {
                if (ts != null) {
                    // 軟刪除：設為停用
                    ts.setServiceStatus(0);
                    technicianServiceRepository.save(ts);
                }
            }
        }
    }

    /**
     * 前台搜尋技師的分頁邏輯
     * 包含：地區篩選、服務複選 (需全數符合)、關鍵字搜尋、平均評分排序、圖片轉換
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TechnicianCardVO> getAllActiveTechnicians(Integer regionCode,
            List<Integer> serviceIds, String keyword, int page, int size, String sort) {
        List<Technician> technicians;

        // 1. 基於時段與地區的初步過濾
        if (serviceIds != null && !serviceIds.isEmpty()) {
            List<Technician> candidates;
            if (regionCode != null) {
                candidates = technicianRepository.findByIsActiveAndRegionCodeValue(1, regionCode);
            } else {
                candidates = technicianRepository.findByIsActive(1);
            }

            // 必須符合「所有」被選中的服務項目
            technicians = candidates.stream().filter(tech -> {
                List<Integer> techServiceIds = technicianServiceRepository
                        .findByTechnicianTechNoAndServiceStatus(tech.getTechNo(), 1)
                        .stream()
                        .map(ts -> ts.getServiceItem().getServiceNo())
                        .collect(Collectors.toList());
                return techServiceIds.containsAll(serviceIds);
            }).collect(Collectors.toList());

        } else if (regionCode != null) {
            technicians = technicianRepository.findByIsActiveAndRegionCodeValue(1, regionCode);
        } else {
            technicians = technicianRepository.findByIsActive(1);
        }

        // 2. 關鍵字過濾 (姓名或服務地區名稱)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            technicians = technicians.stream()
                    .filter(tech -> tech.getRealName().toLowerCase().contains(kw) ||
                            (tech.getServiceArea() != null && tech.getServiceArea().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        // 3. 排序 (目前僅實作評分高低)
        if ("rating_desc".equals(sort)) {
            technicians.sort((t1, t2) -> {
                double r1 = t1.getRatingAmount() > 0 ? (double) t1.getRatingStar() / t1.getRatingAmount() : 0.0;
                double r2 = t2.getRatingAmount() > 0 ? (double) t2.getRatingStar() / t2.getRatingAmount() : 0.0;
                return Double.compare(r2, r1);
            });
        }

        // 4. 手動分頁處理 (PageImpl)
        int totalElements = technicians.size();
        int start = Math.min(page * size, totalElements);
        int end = Math.min((page + 1) * size, totalElements);
        List<Technician> pagedTechnicians = technicians.subList(start, end);

        // 5. Entity 轉 VO 並補充圖片/評分與服務詳情
        List<TechnicianCardVO> content = pagedTechnicians.stream().map(tech -> {
            TechnicianCardVO vo = new TechnicianCardVO();
            vo.setTechNo(tech.getTechNo());
            vo.setRealName(tech.getRealName());
            vo.setPhone(tech.getPhone());
            vo.setEmail(tech.getEmail());
            vo.setServiceArea(tech.getServiceArea());
            vo.setAvgRating(tech.getRatingAmount() > 0 ? (double) tech.getRatingStar() / tech.getRatingAmount() : 0.0);

            if (tech.getTechProfile() != null && tech.getTechProfile().length > 0) {
                vo.setProfilePhotoFromBytes(tech.getTechProfile());
            } else {
                vo.setProfilePhotoFromBytes(loadDefaultProfileImage());
            }

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

        return new org.springframework.data.domain.PageImpl<>(content,
                org.springframework.data.domain.PageRequest.of(page, size), totalElements);
    }

    /**
     * 獲取技師詳細資料與所有過往留言評價
     */
    @Transactional(readOnly = true)
    public TechnicianCardVO getTechnicianDetail(Integer techNo) {
        Technician tech = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));

        TechnicianCardVO vo = new TechnicianCardVO();
        vo.setTechNo(tech.getTechNo());
        vo.setRealName(tech.getRealName());
        vo.setAvgRating(tech.getRatingAmount() > 0 ? (double) tech.getRatingStar() / tech.getRatingAmount() : 0.0);
        vo.setPhone(tech.getPhone());
        vo.setEmail(tech.getEmail());
        vo.setServiceArea(tech.getServiceArea());

        if (tech.getTechProfile() != null && tech.getTechProfile().length > 0) {
            vo.setProfilePhotoFromBytes(tech.getTechProfile());
        } else {
            vo.setProfilePhotoFromBytes(loadDefaultProfileImage());
        }

        // 帶出所有進行中的服務
        List<TechnicianService> services = technicianServiceRepository
                .findByTechnicianTechNoAndServiceStatus(tech.getTechNo(), 1);
        vo.setServices(services.stream().map(ts -> {
            TechnicianCardVO.ServiceVO svo = new TechnicianCardVO.ServiceVO();
            svo.setTsNo(ts.getTsNo());
            svo.setServiceName(ts.getServiceItem().getServiceName());
            svo.setPrice(ts.getPrice());
            return svo;
        }).collect(Collectors.toList()));

        // 帶出評論
        List<com.karshop.install.entity.TechnicianReview> reviews = technicianReviewRepository
                .findByTechnicianTechNo(tech.getTechNo());
        vo.setReviews(reviews.stream().map(r -> new TechnicianCardVO.ReviewVO(
                r.getMember().getMemberName(),
                r.getRatingStar(),
                r.getReviewContent(),
                r.getCreatedAt())).collect(Collectors.toList()));

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
     * 取得技師的所有服務項目
     */
    public List<TechnicianService> getTechnicianServices(Integer techNo) {
        return technicianServiceRepository.findByTechnicianTechNo(techNo);
    }

    /**
     * 管理員核准技師資格
     */
    @Transactional
    public void approveTechnician(Integer techNo) {
        Technician technician = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));
        technician.setIsActive(1); // 1: Active
        technicianRepository.save(technician);
    }

    /**
     * 管理員駁回技師資格
     */
    @Transactional
    public void rejectTechnician(Integer techNo) {
        Technician technician = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));
        technician.setIsActive(2); // 2: Rejected
        technicianRepository.save(technician);
    }

    /**
     * 技師停權機制
     * 條件：必須先處理完所有進行中的訂單才能停權，以確保會員權益。
     */
    @Transactional
    public void suspendTechnician(Integer techNo) {
        long activeOrders = orderRepository.countActiveOrdersByTechnician(techNo);
        if (activeOrders > 0) {
            throw new IllegalStateException("該技師尚有未完成的訂單，無法停權");
        }

        Technician technician = technicianRepository.findById(techNo)
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));
        technician.setIsActive(0); // 設回待審核(停權)狀態
        technicianRepository.save(technician);
    }

    // --- 管理員針對基礎項目的 CRUD (Admin CRUD for items/locations) ---

    @Transactional
    public void createServiceItem(String serviceName, com.karshop.admins.model.AdminVO admin) {
        com.karshop.install.entity.ServiceItem item = new com.karshop.install.entity.ServiceItem();
        item.setServiceName(serviceName);
        item.setAdm(admin);
        item.setCreatedAt(java.time.LocalDateTime.now());
        serviceItemRepository.save(item);
    }

    @Transactional
    public void updateServiceItem(Integer serviceNo, String serviceName) {
        com.karshop.install.entity.ServiceItem item = serviceItemRepository.findById(serviceNo)
                .orElseThrow(() -> new IllegalArgumentException("項目不存在"));
        item.setServiceName(serviceName);
        serviceItemRepository.save(item);
    }

    @Transactional
    public void deleteServiceItem(Integer serviceNo) {
        serviceItemRepository.deleteById(serviceNo);
    }

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
                .orElseThrow(() -> new IllegalArgumentException("據點不存在"));
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
