package com.karshop.report.service;

import com.karshop.report.model.Reports;
import com.karshop.report.repository.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 確保資料完整性
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportsService {

    @Autowired
    private ReportsRepository reportsRepository;

    // ==========================================
    // 1. 前台功能：完全保留原樣，確保前台頁面與提交功能正常
    // ==========================================
    public void submitReport(Reports report) {
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("PENDING");
        report.setAdmNo(1);
        reportsRepository.save(report);
    }

    public List<Reports> getAllReports() {
        return reportsRepository.findAll();
    }

    // ==========================================
    // 2. 後台功能：調整 handleReport 接收 response 參數
    // ==========================================
    @Transactional // 加入事務管理，出錯時會自動回滾
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        // 尋找該筆紀錄，找不到拋出例外
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        // 更新處理資訊
        report.setStatus(status);
        report.setAdmNo(admNo);
        report.setResponse(response); // 💡 存入管理員的回覆內容
        report.setHandled(LocalDateTime.now()); // 紀錄處理時間

        // 存回資料庫
        reportsRepository.save(report);
    }

    // ==========================================
    // 3. 查詢功能
    // ==========================================
    public Reports getOneReport(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }

    public Reports getReportById(Integer id) {
        // 為了讓 Controller 能統一代用，保留此方法
        return reportsRepository.findById(id).orElse(null);
    }
}