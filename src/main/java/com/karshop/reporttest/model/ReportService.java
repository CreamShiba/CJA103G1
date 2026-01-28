package com.karshop.reporttest.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public List<ReportVO> getReportsByStatus(String status) {
        String reportType = "商品";

        if("all".equals(status)){
            return reportRepository.findByReportType(reportType, Sort.by(Sort.Direction.DESC, "reportTime"));
        }else{
            return reportRepository.findByStatusAndReportType(status, reportType);
        }
    }

    public void processReport(Integer reportNo, String newStatus, Integer admNo) {
       ReportVO reportVO =  reportRepository.findById(reportNo).orElseThrow(() -> new RuntimeException("無此檢舉單" + reportNo));
       reportVO.setStatus(newStatus);
       reportVO.setAdmNo(admNo);
       reportVO.setHandledTime(LocalDateTime.now());
        reportRepository.save(reportVO);
    }



}
