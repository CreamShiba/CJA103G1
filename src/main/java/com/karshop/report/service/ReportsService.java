package com.karshop.report.service;

import com.karshop.report.model.Reports;
import com.karshop.report.repository.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportsService {

    @Autowired
    private ReportsRepository reportsRepository;

    // ==========================================
    // 1. 前台功能：提交檢舉
    // ==========================================
    /**
     * ✅ 會員送出檢舉表單時呼叫
     */
    public void submitReport(Reports report) {
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理"); // ✅ 統一使用「待處理」
        report.setAdmNo(1);
        reportsRepository.save(report);

        System.out.println("✅ 新增檢舉成功！編號：" + report.getReportsNo() + "，狀態：" + report.getStatus());
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
     * ✅ 核心優化：後台分頁查詢（簡化狀態判定）
     * @param status 篩選狀態（待處理/已處理）
     * @param page 目前頁碼
     * @param size 每頁幾筆
     * @return 分頁物件
     */
    public Page<Reports> getReportsByStatusWithPagination(String status, int page, int size) {
        // 建立分頁請求，並依照檢舉時間倒序排序 (新的在前)
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());

        System.out.println("💡 Service 查詢狀態：「" + status + "」");

        // ✅ 簡化邏輯：直接使用傳入的狀態值查詢
        // 只接受「待處理」或「已處理」兩種狀態
        return reportsRepository.findByStatus(status, pageable);
    }

    /**
     * ✅ 處理管理員對檢舉案件的審核與結案
     */
    @Transactional
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        // ✅ 更新狀態（應該是「待處理」或「已處理」）
        report.setStatus(status);
        report.setAdmNo(admNo);
        report.setResponse(response);
        report.setHandled(LocalDateTime.now());

        reportsRepository.save(report);

        System.out.println("✅ 檢舉 #" + id + " 已更新為：" + status);
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