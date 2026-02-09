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

        // ✨ 新增連動邏輯：如果檢舉類型是「商品」，嘗試根據名稱自動填入 prod_no
        // 這樣組員的「商品檢舉管理」才能抓到 ID 進行下架或駁回的操作
        if ("商品".equals(report.getReportsType()) && report.getReportsTarget() != null) {
            Integer foundNo = reportsRepository.findProdNoByProdName(report.getReportsTarget());
            if (foundNo != null) {
                report.setProdNo(foundNo);
            }
        }

        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理"); // ✅ 統一使用「待處理」
        report.setAdmNo(1);
        reportsRepository.save(report);

        System.out.println("✅ 新增檢舉成功！編號：" + report.getReportsNo() + "，狀態：" + report.getStatus());
        if (report.getProdNo() != null) {
            System.out.println("🔗 已自動關聯商品編號：" + report.getProdNo() + "，組員現在看得到這筆了！");
        }
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

        // ✅ 核心修正：為了避免組員改狀態名稱導致資料消失，這裡改用「排除法」。
        // 只要狀態不是「待處理」，通通都顯示在已處理分頁中（包含駁回、下架、已處理等）。
        return reportsRepository.findByStatusNot("待處理", pageable);
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

        // ✅ 邏輯優化：如果狀態是「駁回」，則不需要回覆內容也能直接結案
        if ("駁回".equals(status)) {
            report.setStatus("駁回");
            if (response == null || response.trim().isEmpty()) {
                report.setResponse("案件已駁回。");
            } else {
                report.setResponse(response);
            }
        } else {
            // ✅ 更新狀態（應該是「待處理」、「已處理」或「已下架」）
            report.setStatus(status);
            report.setResponse(response);
        }

        report.setAdmNo(admNo);
        report.setHandled(LocalDateTime.now());

        reportsRepository.save(report);

        System.out.println("✅ 檢舉 #" + id + " 已結案，最終狀態為：" + status);
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