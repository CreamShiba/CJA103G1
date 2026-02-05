package com.karshop.report.repository;

import com.karshop.report.model.Reports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}