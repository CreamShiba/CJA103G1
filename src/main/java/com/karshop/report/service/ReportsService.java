package com.karshop.report.service;

import com.karshop.report.model.Reports;
import com.karshop.report.repository.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        report.setStatus("待處理"); // 配合你的 SQL 預設值改為中文
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
     * 💡 核心新增：後台分頁查詢
     * @param status 篩選狀態 (待處理/處理中/已處理/駁回)
     * @param page 目前頁碼
     * @param size 每頁幾筆
     * @return 分頁物件
     */
    public Page<Reports> getReportsByStatusWithPagination(String status, int page, int size) {
        // 建立分頁請求，並依照檢舉時間倒序排序 (新的在前)
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());

        // 💡 修正查詢邏輯以符合 SQL 假資料的狀態
        // 如果是查詢「待處理」標籤頁
        if ("PENDING".equals(status) || "待處理".equals(status)) {
            // 同時抓取資料庫中狀態為「PENDING」、「待處理」以及「處理中」的假資料
            return reportsRepository.findByStatusIn(List.of("PENDING", "待處理", "處理中"), pageable);
        }

        // 如果是查詢「已結案」標籤頁
        if ("已結案".equals(status)) {
            // 同時抓取資料庫中狀態為「已處理」以及「駁回」的假資料
            return reportsRepository.findByStatusIn(List.of("已處理", "駁回", "已結案"), pageable);
        }

        return reportsRepository.findByStatus(status, pageable);
    }

    /**
     * 處理管理員對檢舉案件的審核與結案
     */
    @Transactional // 加入事務管理
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        report.setStatus(status);
        report.setAdmNo(admNo);
        report.setResponse(response);
        report.setHandled(LocalDateTime.now());

        reportsRepository.save(report);
    }

    // ==========================================
    // 3. 查詢功能
    // ==========================================

    public List<Reports> getReportsByMember(Integer memberNo) {
        return reportsRepository.findByMemberNo(memberNo);
    }

    public Reports getReportById(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }
}