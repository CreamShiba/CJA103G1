package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.service.InstallAppealsService; //引用service裡的InstallAppealsService
import org.springframework.beans.factory.annotation.Autowired; //引入自動注入工具。它可以自動幫我把寫好的 Service 實體「裝」進這個 Controller 裡。
import org.springframework.stereotype.Controller; //標記這是一個「控制器」。主要負責接收網址請求，並導向（跳轉）到 templates 裡的 HTML 網頁。
import org.springframework.web.bind.annotation.*; //引入網頁標籤工具（例如 @GetMapping, @PostMapping, @RequestParam 等）。
// 用來定義這段程式是要處理「瀏覽器輸入網址」還是「按下表單送出」。
import org.springframework.ui.Model; //引入模型對象。它像是一個「傳送袋」，讓你把 Java 抓到的資料（如申訴編號）塞進去，傳給 HTML 顯示。
import java.util.List; //引入 Java 標準的清單工具。當你要從資料庫抓「一整群」申訴單時，會用 List 來裝。
import java.time.LocalDateTime;

@Controller //控制器,主要任務是「接聽請求」並「回傳網頁」。
//當你 return "add-appeal" 時，它會去 templates 找 add-appeal.html 顯示出來。
//如果要回傳 HTML 頁面，建議使用 @Controller 而非 @RestController
@RequestMapping("/appeals") //定義這個控制器的「大門口網址」。
//作用：這就像是給這組功能設定一個「分類目錄」。
//所有跟申訴功能有關的網址，開頭都必須是 /appeals。
public class InstallAppealsController {

    @Autowired //自動注入
    private InstallAppealsService installAppealsService;

    // 1. 顯示所有申訴清單 (搭配 JSON 回傳供測試，或導向網頁)
    @GetMapping("/all")
    @ResponseBody // 加上這個可以讓它像昨天一樣回傳 JSON 文字
    public List<InstallAppeals> showAll() {
        return installAppealsService.getAllInstallAppeals();
    }

    // 2. 顯示新增申訴的 HTML 頁面
    // 網址：http://localhost:8080/appeals/add
    @GetMapping("/add")
    public String showAddPage() {
        return "templates-report/add-appeal"; // 這會去 templates 資料夾找 add-appeal.html
    }

    // 3. 處理表單提交
    @PostMapping("/submit")
    public String handleForm(@ModelAttribute InstallAppeals appeal,
                             @RequestParam(value = "type", required = false) String[] types,
                             Model model) {

        // 補上處理多選類別邏輯
        if (types != null && types.length > 0) {
            appeal.setCategories(String.join(",", types));
        }

        // 設定預設值
        appeal.setResponse("尚未回覆");
        appeal.setStatus("待處理");
        appeal.setPriority("一般");

        // 補上資料庫 NOT NULL 必填欄位
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        appeal.setMemberNo(1);
        appeal.setTargetMemberNo(999);
        appeal.setAdmNo(1);

        // 呼叫 Service 存入資料庫
        installAppealsService.submitInstallAppeal(appeal);

        // 關鍵修改：把訂單編號傳給下一頁 (成功頁面)
        model.addAttribute("orderNo", appeal.getInstallOrderNo());

        // 這裡不要用 redirect，直接 return "appeal-success"
        // 這樣瀏覽器才會顯示 templates/appeal-success.html
        return "templates-report/appeal-success";
    }

    //後台管理
    //顯示「安裝申訴」管理列表頁面
    //網址：http://localhost:8080/appeals/admin/list
    @GetMapping("/admin/list")
    public String showInstallAdminPage() {
        return "templates-report/admin-install-list";
    }
    //提供 JSON 資料給後台表格 (供 fetch 使用)
    //網址：http://localhost:8080/appeals/api/all
    @GetMapping("/api/all")
    @ResponseBody
    public List<InstallAppeals> showAllInstallAppeals() {
        return installAppealsService.getAllInstallAppeals();
    }
    //處理管理員結案更新
    //網址：http://localhost:8080/appeals/api/handle
    @PostMapping("api/handle")
    @ResponseBody
    public String handleInstallAppealByAdmin(@RequestParam Integer id,
                                             @RequestParam String response,
                                             @RequestParam String status,
                                             @RequestParam Integer admNo) {
        try {
            installAppealsService.handleInstallAppeal(id, response, status, admNo);
            return "success";
        } catch (Exception e){
            return "error: " + e.getMessage();
        }
    }
}