package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals;
import com.karshop.report.service.InstallAppealsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appeals")
public class InstallAppealsController {
    @Autowired
    private InstallAppealsService installAppealsService;

    @GetMapping("/test")
    public String testSave(){
        InstallAppeals appeal = new InstallAppeals();
        appeal.setInstallOrderNo(20260113); // 模擬安裝訂單編號
        appeal.setDescription("技師安裝過程中刮傷車體，希望能處理。");
        appeal.setMemberNo(1); // 模擬會員編號 (如果是會員，Service 會自動設為 PENDING)
        appeal.setTargetMemberNo(88); // 模擬被申訴的技師編號

        appeal.setResponse("尚未回覆");

        appeal.setAdmNo(1);

        installAppealsService.submitInstallAppeal(appeal); // 呼叫大腦處理存檔
        return "成功存入一筆申訴資料！你可以去 MySQL 看看，或輸入 /appeals/all 查看清單。";
    }

    // B. 【查看功能】：列出資料庫所有的申訴紀錄
    // 網址：http://localhost:8080/appeals/all
    @GetMapping("/all")
    public List<InstallAppeals> showAll() {
        // 呼叫大腦從資料庫撈出所有的申訴紀錄並回傳
        return installAppealsService.getAllInstallAppeals();
    }
}
