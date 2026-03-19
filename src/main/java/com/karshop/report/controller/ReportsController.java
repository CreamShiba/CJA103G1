package com.karshop.report.controller; // 路徑

import com.karshop.report.model.Reports; // 引入Reports VO
import com.karshop.report.service.ReportsService; // 引入ReportsService
import com.karshop.members.model.MembersVO; // 引入會員 VO
import com.karshop.members.model.MembersService; // 導入會員 Service
import org.springframework.beans.factory.annotation.Autowired; // 用來實現依賴注入,自動裝配 Spring Bean,使用者不需要自己new
import org.springframework.data.domain.Page; //引入分頁與排序工具
import org.springframework.jdbc.core.JdbcTemplate; // 導入 JdbcTemplate 用於快速抓取商品名稱,允許開發者直接寫 SQL 語句來操作資料庫。
import org.springframework.stereotype.Controller; // 讓 Spring Boot 知道這個類別用來處理網頁瀏覽器的請求
import org.springframework.ui.Model; // 資料傳遞橋樑：Model 是一個容器，用來封裝 Controller 處理好的業務數據，傳遞給前端頁面顯示。核心方法 addAttribute：通常在 Controller 方法中使用 model.addAttribute("key", value);，將資料放入其中，前端視圖透過 "key" 來取得 value。
import org.springframework.web.bind.annotation.*; // 是 Spring MVC 框架中用於引入處理 Web 請求的註解（Annotation）套件。它允許程式碼使用 @RestController、@RequestMapping、@GetMapping、@PostMapping、@PathVariable、@RequestBody 等註解，以宣告方式定義 RESTful API 的路徑、方法與參數映射。
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 官方術語：重定向屬性,用於顯示操作成功,顯示完後跳轉頁面
import java.time.LocalDateTime; // 提供操作陣列的靜態方法（例如將陣列轉為 List）。
import java.util.Arrays; // 提供操作陣列的靜態方法（如將陣列轉為 List）
import java.util.List; // 定義列表集合，用於儲存多筆資料
import jakarta.servlet.http.HttpSession; // 導入 Session

@Controller // 處理前端資訊傳給後端
@RequestMapping("/reports") // 路由
public class ReportsController {

    @Autowired
    private ReportsService reportsService; // 引入檢舉的service

    @Autowired
    private MembersService membersService; // 注入會員服務

    @Autowired
    private JdbcTemplate jdbcTemplate; // 注入 JDBC 工具

    // 顯示前台新增檢舉頁面, 當有人想看新增檢舉的頁面（輸入 /reports/add），執行這個方法，並把 add-report.html 這個網頁畫面顯示出來。
    @GetMapping("/add")
    public String showReportPage() {    // showReportPage 自定義方法
        return "templates-report/add-report";
    } // return "templates-report/add-report"; 回傳檔案路徑

    // ✅ 處理前台檢舉提交表單（支援從商品頁檢舉後返回原頁面）
    @PostMapping("/submit")                              // 處理 HTTP POST 請求,定義「提交檢舉」的後端窗口路徑
    public String handleReport(
            @ModelAttribute Reports report,                 // @ModelAttribute,把前端資料裝成 Java 看得懂的物件傳進來後端,自動將前端表單內容打包成 Reports 物件
            @RequestParam(required = false) String source,  // ← 抓取網址參數(判斷是從哪個頁面點過來的)
            HttpSession session,                            // ← 從 Session 取得當前登入的會員資料
            RedirectAttributes redirectAttributes,          // ← 用於傳遞訊息,送出後跳出成功，按返回後資料歸零
            Model model){                                   // 把後端處理好的結果傳給前端

        report.setReportsTimestamp(LocalDateTime.now());    // 紀錄儲存時間,自動寫入當下的檢舉時間
        report.setStatus("待處理");                          // 將新案件的初始狀態設為「待處理」

        // 判斷檢舉對象,會員or訪客
        // 從 Session 看有沒有登入中的會員資料
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member != null) {                      // != 不等於,如果 member不等於空值
            report.setMemberNo(member.getMemNo()); // 存入該會員的 ID
            report.setEmail(member.getMemEmail()); // 存入該會員的信箱
        } else {
            // 💡 訪客檢舉：將 memberNo 設為 null
            // 提醒：SQL 中的 member_no 必須拿掉 NOT NULL 否則存檔會失敗
            report.setMemberNo(null);              // 檢舉人 ID 設為空值
        }

        report.setAdmNo(1);

        // Controller 把填好的 report 物件交給 Service,service再去呼叫Repository,執行 SQL指令,好了之後資料存進資料庫
        // Controller -> Service -> Repository -> Database
        reportsService.submitReport(report);


        // 設定成功提示訊息
        redirectAttributes.addFlashAttribute("message", "檢舉已送出，我們會盡快審核！");

        // 判斷來源,如果是從商品頁面送出檢舉過來的
        if ("productDetail".equals(source) && report.getProdNo() != null) {
            // 則自動導回該商品的頁面
            return "redirect:/product/detail?prodNo=" + report.getProdNo();
        }

        // 如果不是從商品頁面送出檢舉,是從一般的檢舉頁面送出檢舉的話,則跳出檢舉成功的頁面
        model.addAttribute("target", report.getReportsTarget());
        return "templates-report/report-success";
    }

    // 定義一個資料接口 (API)，路徑為 /reports/api/all
    @GetMapping("/api/all")
    // 告訴 Spring 直接回傳純資料(JSON)，不要去找 HTML 網頁
    // @ResponseBody Spring Boot語法
    @ResponseBody
    public List<Reports> getAllReportsForAdmin(){
        // 呼叫 Service 撈出資料庫裡「所有的」檢舉紀錄並回傳
        return reportsService.getAllReports();
    }

    // ✅ 處理管理員後台的審核結案動作
    @PostMapping("/api/handle")    // 定義管理員「執行審核」的後端接口路徑
    @ResponseBody                    // 告訴 Spring 直接回傳文字結果("success")，不要跳轉網頁
    public String handleReportByAdmin(@RequestParam Integer id,       // 接收要處理的案件編號
                                      @RequestParam String status,    // 接收處理動作(下架、駁回)
                                      @RequestParam Integer admNo,    // 紀錄管理員編號
                                      @RequestParam(required = false) String response){
        try{
            // 💡 抓取該案件當前狀態
            Reports currentReport = reportsService.getReportById(id);

            // 💡 預設回覆文字
            String finalResponse = (response == null || response.trim().isEmpty()) ? "案件已處理完畢。" : response;
            String finalStatus = "已處理"; // 預設存為已處理

            // 💡 核心修改邏輯：
            // 1. 如果是「駁回」，資料庫狀態存「駁回」，回覆文字加上 [案件駁回]
            // 2. 如果是「下架」，資料庫狀態存「已處理」(避開組員的標籤)，回覆文字加上 [商品下架]
            if (status.equals("駁回") || (response != null && response.contains("駁回"))) {
                finalStatus = "駁回";
                finalResponse = "[案件駁回] " + finalResponse;
            } else if (status.equals("已下架") || status.contains("下架") || (response != null && (response.contains("下架") || response.contains("違規")))) {
                finalStatus = "已處理"; // 💡 存成已處理，組員那邊就不會看到下架標籤
                finalResponse = "[商品下架] " + finalResponse;
            } else {
                finalResponse = "[處理完成] " + finalResponse;
            }

            reportsService.handleReport(id, finalStatus, admNo, finalResponse);
            return "success";
        }catch (Exception e){
            return "error: " + e.getMessage();
        }
    }

    // 顯示管理員後台的「處理案件」詳細頁面
    @GetMapping("/admin/handle")  // 用於查詢或獲取資料,("/指定請求的網址")
    public String showHandlePage(@RequestParam("id") Integer id, Model model) {
        Reports report = reportsService.getReportById(id);      // 呼叫service方法,抓檢舉資料
        if (report != null) {
            model.addAttribute("report", report);  // 由後端丟資料給前端

            // 抓取檢舉人姓名
            // 過濾檢舉人身分,若會員編號有數字且大於0,代表為會員
            if (report.getMemberNo() != null && report.getMemberNo() > 0) {
                // 根據編號去會員資料表找出該人的詳細資料 (跨表查詢)
                MembersVO m = membersService.getOneMember(report.getMemberNo());
                // 顯示文字,組合「姓名」與「ID」，若查無姓名則顯示不明會員
                model.addAttribute("reporterDisplay", (m != null ? m.getMemUsername() : "不明會員") + " (ID: " + report.getMemberNo() + ")");
            } else {
                // 若無會員編號，則統一顯示為訪客
                model.addAttribute("reporterDisplay", "訪客");
            }

            // 抓取被檢舉的商品名稱
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    // 定義 SQL：去 product 表查這編號對應的名字
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    // 執行查詢：jdbcTemplate 會直接回傳一個字串 (String.class)
                    String prodName = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                    // 封裝顯示文字：例如 "米其林輪胎 (編號: 5)"
                    model.addAttribute("targetDisplay", prodName + " (編號: " + report.getProdNo() + ")");
                } catch (Exception e) {
                    // 安全機制：萬一商品被刪除了，查不到名字，就顯示「未知商品」
                    model.addAttribute("targetDisplay", "未知商品 (編號: " + report.getProdNo() + ")");
                }
            } else {
                // 如果是檢舉文章或討論區，就直接拿 report 原有的 target 欄位來顯示
                model.addAttribute("targetDisplay", report.getReportsTarget());
            }

            // 💡 3. 傳遞狀態讓側邊欄 active 正常顯示
            // 修正：只有「待處理」狀態的會在高亮區
            // 抓出這筆檢舉案目前的狀態（例如：待處理、已結案、駁回）
            String s = report.getStatus();
            // 如果狀態是「待處理」，標籤就設為「待處理」
            // 如果是其他（已下架、駁回等），標籤統一設為「已處理」
            String currentStatus = ("待處理".equals(s)) ? "待處理" : "已處理";
            // 把標籤丟給前端
            model.addAttribute("currentStatus", currentStatus);

            return "templates-report/handle-report";
        } else {
            return "redirect:/reports/admin/list";
        }
    }

    // --- 會員中心功能 ---

    /**
     * ✅ 前台顯示檢舉紀錄列表
     */
    @GetMapping("/history")
    public String showReportHistory(Model model) {
        // 為了整合期測試，直接抓取資料庫所有檢舉，讓假資料全部現身
        List<Reports> list = reportsService.getAllReports();

        // 💡 轉換檢舉對象名稱：如果是商品類型就轉換名稱
        for (Reports report : list) {
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    String prodName = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                    report.setReportsTarget(prodName); // 暫存到 Target 欄位供前端顯示
                } catch (Exception e) {
                    report.setReportsTarget("商品編號: " + report.getProdNo());
                }
            }
        }

        model.addAttribute("reports", list);
        return "templates-report/report-history";
    }

    // 顯示檢舉紀錄的詳細資訊頁面
    @GetMapping("/history/detail")
    public String showReportHistoryDetail(@RequestParam("id") Integer id, Model model) {
        Reports report = reportsService.getReportById(id);
        if (report != null) {
            // 💡 轉換詳情頁的對象名稱
            String targetDisplay = report.getReportsTarget();
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    targetDisplay = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                } catch (Exception e) {
                    targetDisplay = "商品編號: " + report.getProdNo();
                }
            }

            model.addAttribute("report", report);
            model.addAttribute("targetDisplay", targetDisplay); // 💡 傳遞名稱到詳情頁
            return "templates-report/report-detail";
        } else {
            return "redirect:/reports/history";
        }
    }

    /**
     * ✅ 核心修改：後台管理列表 (支援 Thymeleaf 與 分頁)
     */
    @GetMapping("/admin/list")
    public String showReportAdminPage(
            @RequestParam(value = "status", defaultValue = "待處理") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // 💡 狀態校準
        String finalStatus = status;
        if (status.equals("PENDING") || status.equals("尚未處理")) {
            finalStatus = "待處理";
        } else if (status.equals("已結案")) {
            finalStatus = "已處理";
        }

        System.out.println("💡 檢舉後台清單 - 查詢狀態校準為：「" + finalStatus + "」，頁碼：「" + page + "」");

        // 強制每頁顯示 6 筆
        int pageSize = 6;
        Page<Reports> reportPage;

        // ✅ 核心調整：
        // 1. 待處理清單：依照組員需求，目前僅顯示真正「待處理」的案件。
        // 2. 已處理清單：顯示所有「已處理」及「駁回」的結案案件。
        if ("待處理".equals(finalStatus)) {
            reportPage = reportsService.getReportsByStatusWithPagination("待處理", page, pageSize);
        } else {
            // ✅ 已處理區抓取結案狀態
            reportPage = reportsService.getReportsByStatusWithPagination("已處理", page, pageSize);
        }

        System.out.println("💡 查詢到 " + reportPage.getTotalElements() + " 筆資料，當前頁有 " + reportPage.getNumberOfElements() + " 筆");

        // 將資料與當前狀態傳回前端
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentStatus", finalStatus);
        model.addAttribute("currentPage", page);

        return "templates-report/admin-report-list";
    }
}