package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals;
import com.karshop.report.service.InstallAppealsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 注意：要跳轉網頁，這裡要改用 @Controller
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.List;

@Controller // 修正：如果要回傳 HTML 頁面，建議使用 @Controller 而非 @RestController
@RequestMapping("/appeals")
public class InstallAppealsController {

    @Autowired
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
        return "add-appeal"; // 這會去 templates 資料夾找 add-appeal.html
    }

    // 3. 處理表單提交
    @PostMapping("/submit")
    public String handleForm(@ModelAttribute InstallAppeals appeal) {
        // 設定預設值，防止資料庫 NOT NULL 報錯
        appeal.setResponse("尚未回覆");
        appeal.setStatus("PENDING");
        appeal.setPriority("MEDIUM");

        installAppealsService.submitInstallAppeal(appeal);
        return "redirect:/appeals/all"; // 成功後跳轉回清單頁
    }
}