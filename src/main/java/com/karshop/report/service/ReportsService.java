package com.karshop.report.service; //路徑

import com.karshop.report.model.Reports; //引入Reports Vo
import com.karshop.report.repository.ReportsRepository; //引入ReportsRepository
import org.springframework.beans.factory.annotation.Autowired; //引入Spring Boot依賴注入
import org.springframework.data.domain.Page; //引入分頁與排序工具
import org.springframework.data.domain.PageRequest; //引入分頁請求工具 //PageRequest 是 Spring Data 框架进行分页查询时的参数构建器 //https://docs.spring.io/spring-data/commons/docs/current/api/index.html?org/springframework/data/domain/PageRequest.html
import org.springframework.data.domain.Pageable; // 分頁查詢和排序功能
import org.springframework.data.domain.Sort; //排序規則,功能：用來設定資料呈現的順序（例：按時間由新到舊排）。
import org.springframework.stereotype.Service; //業務邏輯,大腦
import org.springframework.transaction.annotation.Transactional; //官方術語：宣告式事務管理,要嘛全成功，要嘛全失敗
import java.time.LocalDateTime; //紀錄時間,獲取目前電腦系統的精確時間
import java.util.Arrays; //提供操作陣列的各種方法
import java.util.List; // 引入List

@Service
public class ReportsService {

    @Autowired // 依賴注入
    private ReportsRepository reportsRepository;

    // ==========================================
    // 1. 前台功能：提交檢舉
    // ==========================================
    /**
     * ✅ 會員送出檢舉表單時呼叫
     */
    // 按下送出後會開始跑,如果檢舉的是商品,就用商品名稱去抓商品編號
    public void submitReport(Reports report) {

        // ✨ 新增連動邏輯：如果檢舉類型是「商品」，嘗試根據名稱自動填入 prod_no
        // 這樣組員的「商品檢舉管理」才能抓到 ID 進行下架或駁回的操作
        if ("商品".equals(report.getReportsType()) && report.getReportsTarget() != null) {
            Integer foundNo = reportsRepository.findProdNoByProdName(report.getReportsTarget());
            if (foundNo != null) {
                report.setProdNo(foundNo);
            }
        }

        report.setReportsTimestamp(LocalDateTime.now());    // 抓現在的時間
        report.setStatus("待處理");                          // 預設狀況為待處理
        report.setAdmNo(1);                                 //預設管理員編號為1
        reportsRepository.save(report);                     //將這筆案件存進資料庫
    }

    // ==========================================
    // 2. 後台功能：管理與處理
    // ==========================================

     //取得系統中所有的檢舉案件紀錄
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
        // 建立分頁的規則,控制要看哪一個狀態,第幾頁,一頁顯示幾筆,最新的紀錄會顯示在最上面,適用於後台
        // Sort.by:定義排序規則 Descending (遞減)：從大到小,Ascending (遞增)：從小到大
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());


        // 如果選擇待處理的分頁,就去資料庫抓狀態是待處理的資料
        if ("待處理".equals(status)) {
            return reportsRepository.findByStatus("待處理", pageable);
        }

        // 如果不是要抓待處理,就把除了待處理以外的資料都抓出來
        // 只要狀態不是「待處理」，通通都顯示在已處理分頁中（包含駁回、下架、已處理等）。
        return reportsRepository.findByStatusNot("待處理", pageable);
    }

    /**
     * ✅ 新增：支援多種狀態的分頁查詢
     * 用於將「待處理」、「處理中」、「駁回」、「已下架」合併顯示在同一個清單
     */
    public Page<Reports> getReportsByMultipleStatuses(List<String> statuses, int page, int size) {
        // 建立分頁規則
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportsTimestamp").descending());
        // 呼叫 Repository 的 findByStatusIn
        // 關鍵在「In」：代表只要狀態在傳進來的這個清單（statuses）裡面，都抓出來
        return reportsRepository.findByStatusIn(statuses, pageable);
    }

    /**
     * ✅ 處理管理員對檢舉案件的審核與結案
     */
    //寫這個註解,如果突然出現異常,當機之類,系統會回復原本的狀態,避免發生只有處理一半的狀況
    @Transactional
    public void handleReport(Integer id, String status, Integer admNo, String response) {
        // 根據ID(檢舉編號)去資料庫找那筆檢舉單
        // orElseThrow：如果找不到（例如資料被刪了），就直接報錯跳出
        Reports report = reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        // 如果狀態是「駁回」，則不需要回覆內容也能直接結案
        if ("駁回".equals(status)) {
            report.setStatus("駁回");
            if (response == null || response.trim().isEmpty()) {
                report.setResponse("案件已駁回。");
            } else {
                report.setResponse(response);
            }
        } else {
            // 如果是已處理 已下架,就正常儲存回覆
            report.setStatus(status);
            report.setResponse(response);
        }

        // 紀錄管理員編號以及時間,setAdmNo會自動記錄管理員編號
        report.setAdmNo(admNo);
        report.setHandled(LocalDateTime.now());

        // 儲存進資料庫
        reportsRepository.save(report);
    }

    // ==========================================
    // 3. 查詢功能
    // ==========================================

    // 依照會員編號找資料,前台會員登入後,點選檢舉紀錄,系統把資料抓出來
    public List<Reports> getReportsByMember(Integer memberNo) {
        return reportsRepository.findByMemberNo(memberNo);
    }

    // 前後台共用,查看案件詳情,若找不到則回傳 null，避免程式出錯。
    public Reports getReportById(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }
}