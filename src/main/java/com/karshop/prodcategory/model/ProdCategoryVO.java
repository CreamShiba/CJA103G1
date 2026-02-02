package com.karshop.prodcategory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

@Entity
@Table(name = "product_category")
public class ProdCategoryVO implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column( name = "product_category_no")
  private Integer productCategoryNo;

  @Column( name = "product_category_name")
  @NotEmpty(message = "商品類別名稱: 請勿空白")
  private String productCategoryName;

  public ProdCategoryVO() {
    super();
  }

  public Integer getProductCategoryNo() {
    return productCategoryNo;
  }

  public void setProductCategoryNo(Integer productCategoryNo) {
    this.productCategoryNo = productCategoryNo;
  }

  public String getProductCategoryName() {
    return productCategoryName;
  }

  public void setProductCategoryName(String productCategoryName) {
    this.productCategoryName = productCategoryName;
  }
}
