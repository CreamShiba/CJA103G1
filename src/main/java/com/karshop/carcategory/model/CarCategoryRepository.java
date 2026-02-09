package com.karshop.carcategory.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarCategoryRepository extends JpaRepository<CarCategoryVO, Integer> {
  // 檢查 廠商 + 車名 + 生產區間 是否完全一樣
  boolean existsByMakeAndCarNameAndProdInterval(String make, String carName, String prodInterval);
  //根據「汽車名稱」進行模糊搜尋
  List<CarCategoryVO> findByCarNameContaining(String carName);
}
