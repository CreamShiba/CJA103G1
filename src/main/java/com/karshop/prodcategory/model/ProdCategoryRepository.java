package com.karshop.prodcategory.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdCategoryRepository extends JpaRepository<ProdCategoryVO, Integer> {
  boolean existsByProductCategoryName(String productCategoryName);

  List<ProdCategoryVO> findByProductCategoryNameContaining(String productCategoryName);
}
