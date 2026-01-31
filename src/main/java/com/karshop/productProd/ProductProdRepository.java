package com.karshop.productProd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 第一個參數 Product 是對應的 Entity
 * 第二個參數 Integer 是該 Entity 主鍵 (@Id) 的型別
 */
@Repository
public interface ProductProdRepository extends JpaRepository<ProductProd, Integer> {

    // 如果未來需要根據商品名稱搜尋，可以額外定義：
     List<ProductProd> findByProductNameContaining(String name);
}