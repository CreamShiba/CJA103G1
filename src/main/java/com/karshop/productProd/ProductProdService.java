package com.karshop.productProd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service // 標註為 Spring 管理的 Bean
public class ProductProdService {

    @Autowired
    private ProductProdRepository repository;

    /**
     * 根據商品編號取得單一商品
     * @param prodNo 商品編號
     * @return ProductProd 物件
     */
    public ProductProd getOneProduct(Integer prodNo) {
        Optional<ProductProd> optional = repository.findById(prodNo);
        return optional.orElse(null); // 如果找不到就回傳 null
    }
}
