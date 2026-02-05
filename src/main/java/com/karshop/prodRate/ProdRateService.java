package com.karshop.prodRate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdRateService {

    @Autowired
    private ProdRateRepository repository;

    public void submitRate(ProdRate prodRate) {
        // 首次提交：直接設為 1（已評價一次，可再編輯一次）
        prodRate.setRateStatus(1);
        repository.save(prodRate);
        System.out.println("✅ 首次提交評價 - 商品編號：" + prodRate.getProdNo() +
                "，狀態：1（已評價一次，可再編輯一次）");
    }

    /**
     * 編輯並再次提交評價
     * 從 rate_status = 1 編輯為 rate_status = 2
     * 表示已評價兩次，鎖定不可再編輯
     */
    public void updateRate(ProdRate prodRate) {
        ProdRate existing = repository.findById(prodRate.getProdRateNo()).orElse(null);
        if (existing != null && existing.getRateStatus() == 1) {
            existing.setRate(prodRate.getRate());
            existing.setRateContent(prodRate.getRateContent());
            if (prodRate.getRatePic() != null && prodRate.getRatePic().length > 0) {
                existing.setRatePic(prodRate.getRatePic());
            }
            existing.setRateTime(prodRate.getRateTime());
            // ✅ 編輯並再次提交：設為 2（已評價二次，鎖定）
            existing.setRateStatus(2);
            repository.save(existing);
            System.out.println("✅ 編輯並再次提交評價 - 商品編號：" + existing.getProdNo() +
                    "，狀態：1 → 2（已評價二次，鎖定）");
        } else {
            System.err.println("❌ 無效的編輯：只有狀態 1 的記錄才能編輯");
        }
    }

    public ProdRate getOne(Integer prodRateNo) {
        return repository.findById(prodRateNo).orElse(null);
    }

    public List<ProdRate> getAll() {
        return repository.findAll();
    }

    public ProdRate findByOrdAndProd(Integer ordNo, Integer prodNo) {
        return repository.findByOrdNoAndProdNo(ordNo, prodNo).orElse(null);
    }

    /**
     * 檢查評價是否可編輯
     * rate_status = 0 或 1 時可編輯
     * rate_status = 2 時鎖定
     */
    public boolean isEditable(Integer rateStatus) {
        return rateStatus != null && rateStatus < 2;
    }

    public List<ProdRate> getByMember(Integer memberNo) {
        return repository.findByMemberNo(memberNo);
    }

}