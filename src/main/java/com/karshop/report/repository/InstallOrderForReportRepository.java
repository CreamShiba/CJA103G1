package com.karshop.report.repository;

import com.karshop.report.model.InstallOrderForReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstallOrderForReportRepository extends JpaRepository<InstallOrderForReport, Integer> {
    // 根據會員編號找出所有安裝訂單
    List<InstallOrderForReport> findByMemberNo(Integer memberNo);
}