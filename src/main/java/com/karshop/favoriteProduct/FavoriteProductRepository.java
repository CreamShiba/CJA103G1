package com.karshop.favoriteProduct;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, FavoriteProduct.FavoriteProductId>, JpaSpecificationExecutor<FavoriteProduct> {


    List<FavoriteProduct> findByMemberNo(Integer memberNo);
}