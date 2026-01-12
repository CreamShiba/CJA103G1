package com.karshop.productimage.model;

import com.karshop.product.model.ProductVO;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductImageRepository extends JpaRepository<ProductImageVO, Integer> {

    void deleteByProduct(ProductVO productVO);
}
