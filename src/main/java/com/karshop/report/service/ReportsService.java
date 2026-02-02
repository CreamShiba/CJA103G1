package com.karshop.report.service;

import com.karshop.report.model.Reports;
import com.karshop.report.repository.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 確保資料完整性
import java.time.LocalDateTime;
import java.util.List;

@Service // 標記為服務層，處理業務邏輯與資料搬運
public class ReportsService {

    @Autowired // 自動注入 Repository 連結資料庫
    private ReportsRepository reportsRepository;

    // ==========================================
    // 1. 前台功能：提交檢舉
    // ==========================================
    /**
     * 會員送出檢舉表單時呼叫
     */
    public void submitReport(Reports report) {
        report.setReportsTimestamp(LocalDateTime.now()); // 紀錄檢舉時間
        report.setStatus("PENDING"); // 預設狀態為 PENDING (尚未處理)
        report.setAdmNo(1); // 預設負責管理員編號
        reportsRepository.save(report); // 執行存檔
    }

    // ==========================================
    // 2. 後台功能：管理與處理
    // ==========================================

    /**
     * 取得系統中所有的檢舉案件紀錄
     */
    public List<Reports> getAllReports() {
        return reportsRepository.findAll();
    }

    /**
     * 處理管理員對檢舉案件的審核與結案
     * @param id 檢舉編號
     * @param status 結案狀態 (如: 已結案)
     * @param admNo 處理的管理員編號
     * @param response 管理員回覆內容 (雖然檢舉改用 Email，但資料庫仍可保存紀錄)
     */
    @Transactional // 加入事務管理，確保更新過程若出錯會自動回滾(Rollback)
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        // 尋找該筆紀錄，找不到拋出例外
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        // 更新處理資訊
        report.setStatus(status);
        report.setAdmNo(admNo);
        report.setResponse(response); // 存入管理員的回覆
        report.setHandled(LocalDateTime.now()); // 紀錄處理完成時間

        // 存回資料庫
        reportsRepository.save(report);
    }

    // ==========================================
    // 3. 查詢功能 (包含個人紀錄查詢)
    // ==========================================

    /**
     * 💡 這是讓檢舉紀錄頁面顯示資料的關鍵！
     * 根據會員編號抓取該會員的所有檢舉紀錄
     */
    public List<Reports> getReportsByMember(Integer memberNo) {
        // 呼叫你在 Repository 中定義的新方法
        return reportsRepository.findByMemberNo(memberNo);
    }

    /**
     * 透過 ID 取得單一檢舉案件 (用於處理頁面)
     */
    public Reports getReportById(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }

    /**
     * 舊有方法的別名，維持相容性
     */
    public Reports getOneReport(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }
}