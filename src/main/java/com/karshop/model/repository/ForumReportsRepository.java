package com.karshop.model.repository;

import com.karshop.model.entity.ForumReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumReportsRepository extends JpaRepository<ForumReport, Integer> {

    // 🔍 1. 抓取特定類型的所有檢舉 (例如：FORUM)
    List<ForumReport> findByReportsType(String reportsType);

    // 🔍 2. 抓取特定類型且特定狀態的檢舉
    List<ForumReport> findByReportsTypeAndStatus(String reportsType, String status);
}