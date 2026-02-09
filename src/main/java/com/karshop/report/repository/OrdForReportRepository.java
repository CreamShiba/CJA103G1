package com.karshop.report.repository;

import com.karshop.report.model.OrdForReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdForReportRepository extends JpaRepository<OrdForReport, Integer> {
    // 根據會員編號找出所有商品訂單
    List<OrdForReport> findByMemberNo(Integer memberNo);
}