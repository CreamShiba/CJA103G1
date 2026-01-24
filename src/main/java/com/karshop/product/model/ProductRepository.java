package com.karshop.product.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findBySellerSellerNo(Integer sellerNo);

//   首頁
    Page<ProductVO> findByProdStatus(String prodStatus, Pageable pageable);

//  首頁分類搜尋
    Page<ProductVO> findByProductCategory_ProductCategoryNoAndProdStatus(Integer productCategoryNo, String productStatus, Pageable pageable);

//  首頁關鍵字搜尋
    Page<ProductVO> findByProdNameContainingAndProdStatus(String prodName, String prodStatus, Pageable pageable);

//  賣家中心複合查詢
    @Query("SELECT p FROM ProductVO p WHERE p.seller.sellerNo = :sellerNo " +
            "AND (:prodName IS NULL OR p.prodName LIKE %:prodName%) " +
            "AND (:minPrice IS NULL OR p.prodPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.prodPrice <= :maxPrice) " +
            "AND (:prodStatus IS NULL OR p.prodStatus = :prodStatus) " +
            "ORDER BY p.prodNo DESC")
    List<ProductVO> compositeSearch(@Param("sellerNo") Integer sellerNo,
                                    @Param("prodName") String prodName,
                                    @Param("minPrice") Integer minPrice,
                                    @Param("maxPrice") Integer maxPrice,
                                    @Param("prodStatus") String prodStatus);
}
