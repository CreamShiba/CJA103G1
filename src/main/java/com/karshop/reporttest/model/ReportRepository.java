package com.karshop.reporttest.model;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportVO, Integer> {

    //  查某商品的檢舉 (前台/賣家後台用)
    List<ReportVO> findByProductProdNo(Integer prodNo);

    //  統計某商品被檢舉幾次
    // SQL: SELECT COUNT(*) FROM reports WHERE prod_no = ?
    long countByProductProdNo(Integer prodNo);

    //  查特定狀態的商品檢舉 (管理員後台用)
    List<ReportVO> findByStatusAndReportType(String status, String reportType);

    //  查所有狀態的商品檢舉
    List<ReportVO> findByReportType(String reportType, Sort sort);

    // SQL: SELECT * FROM reports WHERE prod_no = ? ORDER BY reports_timestamp DESC LIMIT 1;
    ReportVO findTopByProductProdNoOrderByReportTimeDesc(Integer prodNo);
}
