package com.karshop.productcategory.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryVO, Integer> {
    // 內建 findAll() 就夠用了
}