package com.karshop.report.repository;

import com.karshop.report.model.Reports; // 2. 引用搬家後的 Reports
import org.springframework.data.jpa.repository.JpaRepository;

// 繼承 JpaRepository 讓 Spring Boot 幫你處理 SQL
public interface ReportsRepository extends JpaRepository<Reports, Integer> {
}