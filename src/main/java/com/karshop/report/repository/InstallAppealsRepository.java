package com.karshop.report.repository;


import com.karshop.report.model.InstallAppeals;
import com.karshop.report.model.ProductAppeals;
import com.karshop.report.model.Reports;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallAppealsRepository extends JpaRepository<InstallAppeals, Integer> {
}

