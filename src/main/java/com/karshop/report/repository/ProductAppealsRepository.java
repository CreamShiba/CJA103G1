package com.karshop.report.repository;

import com.karshop.report.model.ProductAppeals;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductAppealsRepository extends JpaRepository<ProductAppeals, Integer> {

    // 💡 新增：根據會員編號查詢該會員所有的商品申訴案件
    // Spring Data JPA 會根據方法名稱自動生成 SQL：SELECT * FROM product_appeals WHERE member_no = ?
    List<ProductAppeals> findByMemberNo(Integer memberNo);
}