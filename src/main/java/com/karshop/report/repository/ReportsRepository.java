package com.karshop.report.repository;

import com.karshop.report.model.Reports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// 繼承 JpaRepository 讓 Spring Boot 幫你處理 SQL
public interface ReportsRepository extends JpaRepository<Reports, Integer> {

    // 1. 透過會員編號找到該會員的所有檢舉案件 (保留原本功能)
    List<Reports> findByMemberNo(Integer memberNo);

    /**
     * 2. 💡 新增：支援分頁的狀態查詢
     * Spring Data JPA 會自動解析方法名稱，生成：
     * SELECT * FROM reports WHERE status = ? LIMIT 6 OFFSET ?
     */
    Page<Reports> findByStatus(String status, Pageable pageable);

    /**
     * 3. 💡 新增：支援多種狀態的分頁查詢 (用於處理 'PENDING' 與 '待處理' 的相容性)
     */
    Page<Reports> findByStatusIn(List<String> statuses, Pageable pageable);

    /**
     * 4. 💡 新增：為了連動組員的商品管理，根據商品名稱找編號
     * 由於 Reports 直接關聯了 Product，我們可以從這裡反查。
     * 如果你的 Product Entity 類別名稱不是 "Product"，請自行修改下方 JPQL
     */
    @Query(value = "SELECT p.prod_no FROM product p WHERE p.prod_name = :prodName LIMIT 1", nativeQuery = true)
    Integer findProdNoByProdName(@Param("prodName") String prodName);
}