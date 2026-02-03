package com.karshop.carcategory.model;



import java.io.Serializable;
import java.util.List;

import com.karshop.product.model.ProductVO;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name = "car_category")
public class CarCategoryVO implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "car_category_no", updatable = false)
  private Integer carCategoryNo;

  @Column(name = "car_name")
  @NotEmpty(message = "汽車名稱: 請勿空白")
  @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z0-9]+$", message = "汽車名稱: 只能是中、英文字母、數字")
  private String carName;

  @Column(name = "make")
  @NotEmpty(message = "製造廠商: 請勿空白")
  @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z0-9]+$", message = "製造廠商: 只能是中、英文字母、數字")
  private String make;

  @Column(name = "prod_interval")
  @NotEmpty(message = "生產區間: 請勿空白")
  private String prodInterval;

//對映到ProductVO的carCategory
  @OneToMany(mappedBy = "carCategory")
  private List<ProductVO> products;

  public CarCategoryVO() {
    super();
  }

  public Integer getCarCategoryNo() {
    return carCategoryNo;
  }

  public void setCarCategoryNo(Integer carCategoryNo) {
    this.carCategoryNo = carCategoryNo;
  }

  public String getCarName() {
    return carName;
  }

  public void setCarName(String carName) {
    this.carName = carName;
  }

  public String getMake() {
    return make;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public String getProdInterval() {
    return prodInterval;
  }

  public void setProdInterval(String prodInterval) {
    this.prodInterval = prodInterval;
  }

  public List<ProductVO> getProducts() {
    return products;
  }

  public void setProducts(List<ProductVO> products) {
    this.products = products;
  }

  // 顯示結果範例：Toyota - Altis (2019-2023)
  public String getFullName() {
    return this.make + " - " + this.carName + " (" + this.prodInterval + ")";
  }
}
