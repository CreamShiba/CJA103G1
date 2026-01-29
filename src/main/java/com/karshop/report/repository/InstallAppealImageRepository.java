package com.karshop.report.repository;

import com.karshop.report.model.InstallAppealImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallAppealImageRepository extends JpaRepository<InstallAppealImage, Integer> {
    java.util.List<InstallAppealImage> findByAppealsNo(Integer appealsNo);
}

