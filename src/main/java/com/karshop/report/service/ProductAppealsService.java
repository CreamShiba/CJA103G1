package com.karshop.report.service;

import com.karshop.report.model.ProductAppeals;
import com.karshop.report.model.ProductAppealImage;
import com.karshop.report.model.OrdForReport; // 💡 引入妳自己創的 OrdForReport
import com.karshop.report.repository.ProductAppealsRepository;
import com.karshop.report.repository.ProductAppealImageRepository;
import com.karshop.report.repository.OrdForReportRepository; // 💡 引入妳自己創的 Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductAppealsService {

    @Autowired
    private ProductAppealsRepository repository;

    @Autowired
    private ProductAppealImageRepository imageRepository;

    // 💡 改用妳自己建立的 Repository，避免組員的 OrdService 整合問題
    @Autowired
    private OrdForReportRepository ordForReportRepository;

    // ✅ 修改版：新增商品申訴（改用自己的 Repository 驗證）
    public void insert(ProductAppeals productAppeal) {
        // 1️⃣ 驗證訂單是否存在 (使用妳自己的唯讀 Repository)
        boolean exists = ordForReportRepository.existsById(productAppeal.getOrdNo());

        if (!exists) {
            throw new RuntimeException("訂單編號 " + productAppeal.getOrdNo() + " 不存在，無法提交申訴");
        }

        // 💡 提示：因為下拉選單已經在 Controller 根據 memberNo 過濾過了，
        // 這裡可以直接信任傳進來的資料，或是加強驗證該單號是否屬於該會員。

        // 2️⃣ 驗證通過，儲存申訴
        repository.save(productAppeal);

        System.out.println("✅ 商品申訴驗證通過並存檔 - 訂單編號: " + productAppeal.getOrdNo()
                + ", 申訴人: " + productAppeal.getMemberNo());
    }

    // 取得所有商品申訴案件清單
    public List<ProductAppeals> getAll() {
        return repository.findAll();
    }

    // 根據 ID 取得特定的商品申訴案件
    public ProductAppeals getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // 根據會員編號取得商品申訴紀錄
    public List<ProductAppeals> getByMemberNo(Integer memberNo) {
        return repository.findByMemberNo(memberNo);
    }

    // 處理商品申訴（管理員回覆與狀態變更）
    public void handleProductAppeal(Integer id, String response, String status, Integer admNo) {
        ProductAppeals appeal = repository.findById(id).orElse(null);
        if (appeal != null) {
            appeal.setResponse(response);
            appeal.setStatus(status);
            appeal.setAdmNo(admNo);
            appeal.setUpdatedDate(LocalDateTime.now());
            appeal.setProcessDate(LocalDateTime.now());
            repository.save(appeal);
        }
    }

    // 儲存單張商品申訴圖片
    public void saveAppealImage(Integer appealsNo, byte[] imageData) {
        ProductAppealImage image = new ProductAppealImage();
        image.setAppealsNo(appealsNo);
        image.setImage(imageData);
        image.setCreatedAt(LocalDateTime.now());
        imageRepository.save(image);
    }

    // 根據申訴編號取得該案件的所有圖片清單
    public List<ProductAppealImage> getImagesByAppealsNo(Integer appealsNo) {
        return imageRepository.findByAppealsNo(appealsNo);
    }

    // 根據圖片編號取得圖片二進位內容
    public byte[] getAppealImageById(Integer imgNo) {
        return imageRepository.findById(imgNo)
                .map(ProductAppealImage::getImage)
                .orElse(null);
    }
}