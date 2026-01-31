package com.karshop.productProd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductImgService {

    @Autowired
    private ProductImgRepository repository;

    // 取得單張圖片 (供 DBGifReader 使用)
    public ProductImg getOneProductImage(Integer imgNo) {
        return repository.findById(imgNo).orElse(null);
    }

    // 取得該商品的所有圖片
    public List<ProductImg> getByProdNo(Integer prodNo) {
        return repository.findByProdNo(prodNo);
    }
}
