package com.karshop.coupon;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
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
            predicates.add(cb.equal(root.get("couponStatus"), 1));

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
        couponRepository.deleteById(couponNo);
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


}
