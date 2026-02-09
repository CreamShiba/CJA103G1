package com.karshop.coupon;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> findByCompositeQuery(String title, String content, LocalDateTime start, LocalDateTime end) {
        return couponRepository.findAll((Specification<Coupon>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //  強制加入狀態過濾：只顯示狀態為 1 (有效) 的資料
//            predicates.add(cb.equal(root.get("couponStatus"), 1));

            //  標題模糊查詢 (LIKE %...%)
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(root.get("couponTitle"), "%" + title + "%"));
            }

            //  內容模糊查詢
            if (content != null && !content.trim().isEmpty()) {
                predicates.add(cb.like(root.get("couponContent"), "%" + content + "%"));
            }

            //  日期區間查詢 (couponStart >= start AND couponEnd <= end)
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("couponStart"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("couponEnd"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

    }

    /**
     * 取得所有優惠券：過濾掉已失效 (status=0) 的資料
     */
    public List<Coupon> getAll() {
        return couponRepository.findAll().stream()
                .filter(c -> c.getCouponStatus() != null && c.getCouponStatus() == 1)
                .collect(Collectors.toList());
    }

    public Coupon getOne(Integer couponNo) {
        return couponRepository.findById(couponNo).orElse(null);
    }

    public void delete(Integer couponNo){
        // 1. 先找出該筆資料
        Coupon coupon = couponRepository.findById(couponNo).orElse(null);

        if (coupon != null) {
            // 2. 將狀態改為 0 (註銷/刪除)
            coupon.setCouponStatus(0);

            // 3. 儲存回資料庫 (save 會自動判斷為 Update)
            couponRepository.save(coupon);
        }
    }

    public void insert(Coupon coupon){
        // 新增時確保初始狀態為 1
        if (coupon.getCouponStatus() == null) {
            coupon.setCouponStatus(1);
        }
        couponRepository.save(coupon);
    }

    public void update(Coupon coupon){
        couponRepository.save(coupon);
    }

    // 新增：取得分頁資料
    public Page<Coupon> getAllPaged(int page, int size) {
        // 設定排序方式（例如按編號倒序），頁數從 0 開始計算
        Pageable pageable = PageRequest.of(page, size, Sort.by("couponNo").descending());
        // 這裡若要維持「只顯示狀態為 1」的邏輯，建議在 Repository 定義 findByCouponStatus(Integer status, Pageable pageable)
        // 簡單起見，我們先示範全量分頁
        return couponRepository.findAll(pageable);
    }


}
