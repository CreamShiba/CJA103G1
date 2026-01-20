package com.karshop.productcategorytest.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryRepository repository;

    public List<ProductCategoryVO> getAll() {
        return repository.findAll();
    }
}