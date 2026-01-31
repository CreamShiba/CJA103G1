package com.karshop.productProd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImgRepository extends JpaRepository<ProductImg, Integer> {
    // 評價頁面查詢該商品的所有圖片
    List<ProductImg> findByProdNo(Integer prodNo);
}
