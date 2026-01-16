package com.karshop.report.repository;


import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.model.ProductAppeals; //引用model裡的ProductAppeals
import com.karshop.report.model.Reports; //引用model裡的Reports
import org.springframework.data.jpa.repository.JpaRepository; //引用JPA

public interface InstallAppealsRepository extends JpaRepository<InstallAppeals, Integer> {
}

