package com.karshop.report.service;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.repository.InstallAppealsRepository; //引用repository裡的InstallAppeals
import org.springframework.beans.factory.annotation.Autowired; //引入自動注入，用來將 Repository 自動裝配進這個 Service 中。
import org.springframework.stereotype.Service; //標記這是一個服務層。讓 Spring Boot 知道這個類別是用來寫邏輯、算資料的地方。

import java.time.LocalDateTime; //引入時間工具，用來在存檔時紀錄當下的日期與時間。
import java.util.List; //引入清單工具，用來裝載從資料庫抓回來的多筆申訴紀錄。

@Service
public class InstallAppealsService {
    @Autowired
    private InstallAppealsRepository installAppealsRepository;

    public void submitInstallAppeal(InstallAppeals appeal) {

        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        //設定申請與更新時間

        if (appeal.getMemberNo() == null || appeal.getMemberNo() <= 0) {
            appeal.setStatus("IGNORED");
            appeal.setPriority("NONE");
            appeal.setResponse("系統:非會員申訴，不進入人工處理流程。");
        } else {
            appeal.setStatus("PENDING");

            if(appeal.getDescription().contains("壞") || appeal.getDescription().contains("安全")) {
                appeal.setPriority("HIGH");
            } else {
                appeal.setPriority("MEDIUM");
            }
        }

        // 4. 存檔入庫
        installAppealsRepository.save(appeal);
    }

    // 後台管理員查詢所有安裝申訴案件
    public List<InstallAppeals> getAllInstallAppeals() {
        return installAppealsRepository.findAll();
    }
}

