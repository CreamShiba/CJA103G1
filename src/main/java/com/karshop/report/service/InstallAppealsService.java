package com.karshop.report.service;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.model.InstallAppealImage; //引用圖片model
import com.karshop.report.repository.InstallAppealsRepository; //引用repository裡的InstallAppeals
import com.karshop.report.repository.InstallAppealImageRepository; //引用圖片專用repository
import org.springframework.beans.factory.annotation.Autowired; //引入自動注入，用來將 Repository 自動裝配進這個 Service 中。
import org.springframework.stereotype.Service; //標記這是一個服務層。讓 Spring Boot 知道這個類別是用來寫邏輯、算資料的地方。
import java.time.LocalDateTime; //引入時間工具，用來在存檔時紀錄當下的日期與時間。
import java.util.List; //引入清單工具，用來裝載從資料庫抓回來的多筆申訴紀錄。

@Service
public class InstallAppealsService {
    @Autowired
    private InstallAppealsRepository installAppealsRepository;

    @Autowired // 💡 注入圖片專用的 Repository 才能執行存檔動作
    private InstallAppealImageRepository installAppealImageRepository;

    public void submitInstallAppeal(InstallAppeals appeal) {
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        //設定申請與更新時間

        if (appeal.getMemberNo() == null || appeal.getMemberNo() <= 0) {
            appeal.setStatus("待處理"); // 💡 改成中文
            appeal.setPriority("NONE");
            appeal.setResponse("系統:非會員申訴，不進入人工處理流程。");
        } else {
            appeal.setStatus("待處理"); // 💡 改成中文

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

    // 處理管理員結案更新：更新狀態、回覆內容與處理時間
    public void handleInstallAppeal(Integer id, String response, String status, Integer admNo) {
        InstallAppeals appeal = installAppealsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的申訴紀錄"));

        // 更新資料(對應SQL欄位)
        appeal.setResponse(response); //管理員回復的文字
        appeal.setStatus(status); //狀態(ex:已處理)
        appeal.setAdmNo(admNo); //紀錄是哪個管理員處理的
        appeal.setProcessDate(LocalDateTime.now()); //處理日期
        appeal.setUpdatedDate(LocalDateTime.now()); //最後更新日期

        //存回資料庫
        installAppealsRepository.save(appeal);
    }

    public InstallAppeals getAppealById(Integer id) {
        return installAppealsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的申訴案件"));
    }

    // 💡 儲存圖片的方法
    public void saveAppealImage(Integer appealsNo, byte[] imageBytes) {
        InstallAppealImage img = new InstallAppealImage();
        img.setAppealsNo(appealsNo);
        img.setImage(imageBytes);

        // 💡 執行存檔：把圖片物件存進 install_appeal_images 表
        installAppealImageRepository.save(img);
    }

    // 💡 透過 img_no 取得圖片二進位資料
    public byte[] getAppealsImageById(Integer imgNo) {
        return installAppealImageRepository.findById(imgNo)
                .map(img -> img.getImage())
                .orElse(null);
    }

    //需要一個方法「找出某個申訴案件的所有圖片編號」
    // 這要在 install_appeal_images 表找 appeals_no = ? 的所有紀錄
    // 假設你在 Repository 已經寫好 findByAppealsNo 方法
    public List<InstallAppealImage> getImagesByAppealsNo(Integer appealsNo) {
        // 這裡我們等一下要在 Repository 補上一行查詢指令
        return installAppealImageRepository.findByAppealsNo(appealsNo);
    }

    //這裡跟申訴紀錄有關 根據會員編號查詢
    public List<InstallAppeals> getAppealsByMember(Integer memberNo) {
        return installAppealsRepository.findByMemberNo(memberNo);
    }
}