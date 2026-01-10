package com.karshop.productimage.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class ProductImageService {

    @Autowired
    ProductImageRepository productImageRepository;

    public void addImages(ProductImageVO piVO){
        productImageRepository.save(piVO);
    }

    public void updateImages(ProductImageVO piVO){
        productImageRepository.save(piVO);
    }

    public void deleteImages(ProductImageVO piVO){
        productImageRepository.delete(piVO);
    }

    public ProductImageVO getOneImage(Integer imgNo){
        return productImageRepository.findById(imgNo).orElse(null);
    }

}