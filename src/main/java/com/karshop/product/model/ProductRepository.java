package com.karshop.product.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findBySellerNo(Integer sellerNo);

    List<ProductVO> findByProdStatus(String prodStatus);

    List<ProductVO> findByProdNameContainingAndProdStatus(String prodName,String prodStatus);
}
