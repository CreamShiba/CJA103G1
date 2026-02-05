package com.karshop.report.service;

import com.karshop.report.model.InstallAppeals;
import com.karshop.report.model.InstallAppealImage;
import com.karshop.report.repository.InstallAppealsRepository;
import com.karshop.report.repository.InstallAppealImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstallAppealsService {
    @Autowired
    private InstallAppealsRepository installAppealsRepository;

    @Autowired
    private InstallAppealImageRepository installAppealImageRepository;

    // ✅ 提交安裝申訴
    public void submitInstallAppeal(InstallAppeals appeal) {
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());

        // ✅ 統一使用中文狀態：「待處理」
        if (appeal.getMemberNo() == null || appeal.getMemberNo() <= 0) {
            appeal.setStatus("待處理");
            appeal.setPriority("NONE");
            appeal.setResponse("系統:非會員申訴，不進入人工處理流程。");
        } else {
            appeal.setStatus("待處理");

            // 根據描述內容判斷優先級
            if (appeal.getDescription() != null &&
                    (appeal.getDescription().contains("壞") || appeal.getDescription().contains("安全"))) {
                appeal.setPriority("HIGH");
            } else {
                appeal.setPriority("MEDIUM");
            }
        }

        // 存檔入庫
        installAppealsRepository.save(appeal);
    }

    // ✅ 後台管理員查詢所有安裝申訴案件
    public List<InstallAppeals> getAllInstallAppeals() {
        return installAppealsRepository.findAll();
    }

    // ✅ 處理管理員結案更新：更新狀態、回覆內容與處理時間
    public void handleInstallAppeal(Integer id, String response, String status, Integer admNo) {
        InstallAppeals appeal = installAppealsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的申訴紀錄"));

        // ✅ 更新資料（只接受「待處理」或「已處理」）
        appeal.setResponse(response);
        appeal.setStatus(status); // 應該是「待處理」或「已處理」
        appeal.setAdmNo(admNo);
        appeal.setProcessDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());

        // 存回資料庫
        installAppealsRepository.save(appeal);

        System.out.println("✅ 申訴 #" + id + " 已更新為：" + status);
    }

    // ✅ 根據 ID 取得單一申訴案件
    public InstallAppeals getAppealById(Integer id) {
        return installAppealsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的申訴案件"));
    }

    // ✅ 儲存圖片的方法
    public void saveAppealImage(Integer appealsNo, byte[] imageBytes) {
        InstallAppealImage img = new InstallAppealImage();
        img.setAppealsNo(appealsNo);
        img.setImage(imageBytes);

        // 執行存檔：把圖片物件存進 install_appeal_images 表
        installAppealImageRepository.save(img);
    }

    // ✅ 透過 img_no 取得圖片二進位資料
    public byte[] getAppealsImageById(Integer imgNo) {
        return installAppealImageRepository.findById(imgNo)
                .map(img -> img.getImage())
                .orElse(null);
    }

    // ✅ 找出某個申訴案件的所有圖片
    public List<InstallAppealImage> getImagesByAppealsNo(Integer appealsNo) {
        return installAppealImageRepository.findByAppealsNo(appealsNo);
    }

    // ✅ 根據會員編號查詢該會員的所有申訴
    public List<InstallAppeals> getAppealsByMember(Integer memberNo) {
        return installAppealsRepository.findByMemberNo(memberNo);
    }
}