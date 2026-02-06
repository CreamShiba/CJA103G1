package com.karshop.faq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service("faqService")
public class faqservice {

    @Autowired
    private faqrepository repository;
    /**
     * 取得所有已發布的 FAQ（前台用）
     */
    public List<faq> getPublishedFaqs() {
        return repository.findByStatusOrderBycreate_dateDesc("已發佈");
    }

    /**
     * 取得所有 FAQ，包含草稿（後台用）
     */
    public List<faq> getAllFaqs() {
        return repository.findAllOrderBycreate_dateDesc();
    }

    /**
     * 根據 ID 取得單一 FAQ
     */
    public faq getFaqById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 新增 FAQ
     * 自動設定建立時間和更新時間
     */
    public faq createFaq(faq entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreate_date(now);
        entity.setUpdated_date(now);
        return repository.save(entity);
    }

    /**
     * 更新 FAQ
     * 保留原有的 create_date，只更新其他欄位
     */
    public faq updateFaq(Integer id, faq entity) {
        return repository.findById(id).map(existingFaq -> {
            // 只更新需要變更的欄位
            existingFaq.setQuestion(entity.getQuestion());
            existingFaq.setAnswer(entity.getAnswer());
            existingFaq.setStatus(entity.getStatus());
            // adm_no 通常不需要更新，但如果需要可以加上
            // existingFaq.setAdm_no(entity.getAdm_no());

            // updated_date 會由 @UpdateTimestamp 自動處理
            // create_date 因為 updatable=false 不會被改變

            return repository.save(existingFaq);
        }).orElse(null);
    }

    /**
     * 刪除 FAQ
     */
    public boolean deleteFaq(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}