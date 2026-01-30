package com.karshop.productimage.model;

import com.karshop.product.model.ProductVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
public class ProductImageService {

    @Autowired
    ProductImageRepository productImageRepository;

    @Transactional
    public void addImages(ProductImageVO piVO){
        piVO.setUploadDate(LocalDate.now());
        productImageRepository.save(piVO);
    }

    public void updateImages(ProductImageVO piVO){
        productImageRepository.save(piVO);
    }

    public void deleteImage(Integer imgNo){
        productImageRepository.deleteById(imgNo);
    }

    public ProductImageVO getOneImage(Integer imgNo){
        return productImageRepository.findById(imgNo).orElse(null);
    }

    public void deleteByProduct(ProductVO productVO){
        productImageRepository.deleteByProduct(productVO);
    }

}