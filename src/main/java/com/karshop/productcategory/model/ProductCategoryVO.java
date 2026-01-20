package com.karshop.productcategory.model;

import jakarta.persistence.*;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore; // 避免無窮迴圈
import com.karshop.product.model.ProductVO;

@Entity
@Table(name = "product_category")
public class ProductCategoryVO implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_no")
    private Integer productCategoryNo;

    @Column(name = "product_category_name")
    private String productCategoryName;

    // 🔥 新增：雙向關聯 (一個分類對應多個商品)
    // mappedBy = "productCategory" 指的是 ProductVO 裡面的那個變數名稱
    // CascadeType.ALL 視需求加，通常查詢不需要
    @OneToMany(mappedBy = "productCategory", cascade = CascadeType.ALL)
    @JsonIgnore // 🔥 非常重要！避免轉 JSON 時產生無窮迴圈 (分類->商品->分類->商品...)
    private Set<ProductVO> products;

    public ProductCategoryVO() {}

    // Getter / Setter
    public Integer getProductCategoryNo() { return productCategoryNo; }
    public void setProductCategoryNo(Integer productCategoryNo) { this.productCategoryNo = productCategoryNo; }

    public String getProductCategoryName() { return productCategoryName; }
    public void setProductCategoryName(String productCategoryName) { this.productCategoryName = productCategoryName; }

    public Set<ProductVO> getProducts() { return products; }
    public void setProducts(Set<ProductVO> products) { this.products = products; }
}