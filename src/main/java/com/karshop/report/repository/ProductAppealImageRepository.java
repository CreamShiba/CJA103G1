package com.karshop.report.repository;

import com.karshop.report.model.ProductAppealImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductAppealImageRepository extends JpaRepository<ProductAppealImage, Integer> {

    // 根據申訴案件編號查詢所有圖片
    List<ProductAppealImage> findByAppealsNo(Integer appealsNo);
}