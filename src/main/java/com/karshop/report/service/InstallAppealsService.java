package com.karshop.report.service;

import com.karshop.report.model.InstallAppeals;
import com.karshop.report.repository.InstallAppealsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

