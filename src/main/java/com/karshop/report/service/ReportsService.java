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
import java.util.Arrays;
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
     * ✅ 核心優化：後台分頁查詢（強化攔截版）
     * @param status 篩選狀態（待處理/已處理）
     * @param page 目前頁碼
     * @param size 每頁幾筆
     * @return 分頁物件
     */
    public Page<Reports> getReportsByStatusWithPagination(String status, int page, int size) {
        // 建立分頁請求，並依照檢舉時間倒序排序 (新的在前)
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());

        System.out.println("💡 Service 查詢狀態：「" + status + "」");

        // ✅ 核心修正：依照組員需求，待處理區僅顯示「待處理」案件。
        if ("待處理".equals(status)) {
            return reportsRepository.findByStatus("待處理", pageable);
        }

        // ✅ 核心修正：為了避免組員頁面出現「下架」標籤，已處理區查詢應排除「下架」字眼。
        // 但為了確保「已處理」分頁能撈到所有結案資料，這裡必須包含所有結案狀態字眼（含 DB 現有的下架資料）。
        // 這樣你在報表清單才看得到編號 #3 那些案件。
        List<String> closedStatuses = Arrays.asList("已處理", "已結案", "駁回", "下架", "已下架");
        return reportsRepository.findByStatusIn(closedStatuses, pageable);
    }

    /**
     * ✅ 新增：支援多種狀態的分頁查詢
     * 用於將「待處理」、「處理中」、「駁回」、「已下架」合併顯示在同一個清單
     */
    public Page<Reports> getReportsByMultipleStatuses(List<String> statuses, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());
        return reportsRepository.findByStatusIn(statuses, pageable);
    }

    /**
     * ✅ 處理管理員對檢舉案件的審核與結案
     */
    @Transactional
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        // ✅ 更新狀態（應該是「待處理」、「已處理」、「駁回」或「已下架」）
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