package com.karshop.product.model;

import com.karshop.productimage.model.ProductImageVO;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public void addProduct(ProductVO prodVO){
        productRepository.save(prodVO);
    }

    public void updateProduct(ProductVO prodVO){
        productRepository.save(prodVO);
    }

    public void deleteProduct(Integer prodNo){
        productRepository.deleteById(prodNo);
    }

    public ProductVO getOneProduct(Integer prodNo){
        return productRepository.findById(prodNo).orElse(null);
    }

    public List<ProductVO> getAllProducts(){
        return productRepository.findAll();
    }

    public List<ProductVO> getProductsBySellerNo(Integer sellerNo){
        return productRepository.findBySellerSellerNo(sellerNo);
    }

    //  商城複合查詢
    public Page<ProductVO> getAllForBuyer(int pageNumber, String keyword, Integer productCategoryNo, Integer sellerNo, Integer minPrice, Integer maxPrice){
        // 設定分頁：
        // pageNumber - 1 : 因為 Spring 的頁數是從 0 開始算，但網址我們通常傳 1
        // 9 : 每頁顯示 9 筆
        // Sort : 按照商品編號 (prodNo) 降序排列 (最新的在最前面)
        Pageable pageable = PageRequest.of(pageNumber - 1, 9, Sort.by("prodNo").descending());

        if(keyword != null && keyword.trim().isEmpty()){
            keyword = null;
        }

        if(minPrice != null && maxPrice != null && minPrice > maxPrice){
            Integer maxPriceTemp = minPrice;
            minPrice = maxPrice;
            maxPrice = maxPriceTemp;
        }

        return productRepository.findAllForBuyer(keyword, productCategoryNo, sellerNo, minPrice, maxPrice, pageable);

    }


    public List<ProductVO> getProductBySearchForSeller(Integer sellerNo, String prodName, Integer minPrice, Integer maxPrice, String prodStatus){
        if(prodName != null && prodName.trim().isEmpty()){
            prodName = null;
        }

        if(prodStatus != null && prodStatus.trim().isEmpty()){
            prodStatus = null;
        }

        if(minPrice != null && maxPrice != null && minPrice > maxPrice){
            Integer maxPriceTemp = minPrice;
            minPrice = maxPrice;
            maxPrice = maxPriceTemp;
        }

        return productRepository.compositeSearch(sellerNo, prodName, minPrice, maxPrice, prodStatus);
    }

}
