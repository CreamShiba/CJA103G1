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
}
