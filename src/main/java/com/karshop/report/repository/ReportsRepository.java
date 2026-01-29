package com.karshop.report.repository;

import com.karshop.report.model.Reports; // 2. 引用搬家後的 Reports
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// 繼承 JpaRepository 讓 Spring Boot 幫你處理 SQL
public interface ReportsRepository extends JpaRepository<Reports, Integer> {
    // 透過會員編號找到該會員的所有檢舉案件
    List<Reports> findByMemberNo(Integer memberNo);
}