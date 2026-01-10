package com.karshop.product.model;

import com.karshop.productimage.model.ProductImageVO;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

//    @Autowired
//    private SessionFactory sessionFactory;

    public void addProduct(ProductVO prodVO){
        productRepository.save(prodVO);
    }

    public void updateProduct(ProductVO prodVO){
        ProductVO productVO = new ProductVO();
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
        return productRepository.findBySellerNo(sellerNo);
    }

    public List<ProductVO> getProductsByProdStatus(String prodStatus){
        return productRepository.findByProdStatus(prodStatus);
    }

    public List<ProductVO> getProductsByNameContainingAndProdStatus(String prodName,String prodStatus){
        return productRepository.findByProdNameContainingAndProdStatus(prodName,prodStatus);
    }


}
