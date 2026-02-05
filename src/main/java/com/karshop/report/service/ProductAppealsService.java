package com.karshop.report.service;

import com.karshop.report.model.ProductAppeals;
import com.karshop.report.model.ProductAppealImage; // 💡 引用圖片 Model
import com.karshop.report.repository.ProductAppealsRepository;
import com.karshop.report.repository.ProductAppealImageRepository; // 💡 引用圖片 Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 💡 建議加上事務管理
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional // 💡 確保資料庫操作的完整性
public class ProductAppealsService {

    @Autowired
    private ProductAppealsRepository repository;

    @Autowired
    private ProductAppealImageRepository imageRepository; // 💡 自動注入圖片 Repository

    // 1. 儲存商品申訴案件
    public void insert(ProductAppeals productAppeal) {
        repository.save(productAppeal);
    }

    // 2. 取得所有商品申訴案件清單
    public List<ProductAppeals> getAll() {
        return repository.findAll();
    }

    // 3. 💡 根據 ID 取得特定的商品申訴案件
    public ProductAppeals getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // 💡 根據會員編號取得商品申訴紀錄 (用於前台申訴紀錄頁面)
    public List<ProductAppeals> getByMemberNo(Integer memberNo) {
        return repository.findByMemberNo(memberNo);
    }

    // 4. 💡 處理商品申訴（管理員回覆與狀態變更）
    public void handleProductAppeal(Integer id, String response, String status, Integer admNo) {
        ProductAppeals appeal = repository.findById(id).orElse(null);
        if (appeal != null) {
            appeal.setResponse(response);
            appeal.setStatus(status);
            appeal.setAdmNo(admNo);
            appeal.setUpdatedDate(LocalDateTime.now());
            appeal.setProcessDate(LocalDateTime.now()); // 💡 設定案件處理時間
            repository.save(appeal);
        }
    }

    // --- 💡 以下為整合進來的圖片存取功能 ---

    // 5. 💡 儲存單張商品申訴圖片
    public void saveAppealImage(Integer appealsNo, byte[] imageData) {
        ProductAppealImage image = new ProductAppealImage();
        image.setAppealsNo(appealsNo); // 💡 關聯申訴案件編號
        image.setImage(imageData); // 💡 存入圖片二進位資料
        image.setCreatedAt(LocalDateTime.now()); // 💡 設定上傳時間
        imageRepository.save(image);
    }

    // 6. 💡 根據申訴編號取得該案件的所有圖片清單
    public List<ProductAppealImage> getImagesByAppealsNo(Integer appealsNo) {
        return imageRepository.findByAppealsNo(appealsNo);
    }

    // 7. 💡 根據圖片編號取得圖片二進位內容（用於網頁顯示圖片）
    public byte[] getAppealImageById(Integer imgNo) {
        return imageRepository.findById(imgNo)
                .map(ProductAppealImage::getImage)
                .orElse(null);
    }
}