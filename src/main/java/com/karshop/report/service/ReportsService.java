package com.karshop.report.service;

import com.karshop.report.model.Reports;
import com.karshop.report.repository.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportsService {

    @Autowired
    private ReportsRepository reportsRepository;

    public void submitReport(Reports report) {
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("PENDING");
        report.setAdmNo(1);
        reportsRepository.save(report);
    }
    public List<Reports> getAllReports() {
        return reportsRepository.findAll();
    }

    public void handleReport(Integer id, String status, Integer admNo){
        Reports report =  reportsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的檢舉紀錄"));

        report.setStatus(status);
        report.setAdmNo(admNo);
        report.setHandled(LocalDateTime.now());
        reportsRepository.save(report);
    }

    public Reports getOneReport(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }
}
