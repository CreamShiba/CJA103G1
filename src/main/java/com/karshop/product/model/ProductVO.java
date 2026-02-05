package com.karshop.product.model;

import com.karshop.buyerrating.model.BuyerRatingVO;
import com.karshop.carcategory.model.CarCategoryVO;
import com.karshop.orddetail.model.OrdDetailVO;
import com.karshop.productcategorytest.model.ProductCategoryVO;
import com.karshop.productimage.model.ProductImageVO;
import com.karshop.sellertest.model.SellerVO;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "product")
public class ProductVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_no")
    Integer prodNo;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY 建議加上，效能較好
    @JoinColumn(name = "product_category_no") // 對應資料庫的外來鍵欄位名稱
    private ProductCategoryVO productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_category_no")
    private CarCategoryVO carCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_no")
    private SellerVO seller;

    @Column(name = "prod_name")
    @NotBlank(message = "請輸入商品名稱")
    @Size(min = 2, max = 30, message = "商品名稱長度須在 2 至 30 字之間")
    String prodName;

    @Column(name = "prod_desc")
    @NotBlank(message = "請填寫商品描述")
    String prodDesc;

    @Column(name = "prod_price")
    @NotNull(message = "請填寫商品價格")
    @Min(value = 10, message = "商品價格不可小於${value}")
    Integer prodPrice;

    @Column(name = "prod_status")
    String prodStatus = "上架中"; //商品狀態初始為"上架中"

    @Column(name = "rating_amount")
    Integer ratingAmount = 0; //評分總人數初始為0

    @Column(name = "rating_star")
    Integer ratingStar = 0;  //評分總星數初始為0

    @Column(name = "prod_qty")
    @NotNull(message = "請填寫庫存數量")
    @Min(value = 1, message = "庫存數量不可小於0")
    Integer prodQty;

    @OneToMany(mappedBy ="product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ProductImageVO> productImage;

    @OneToMany(mappedBy ="product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrdDetailVO> orderDetail;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BuyerRatingVO> buyerRatings;

    public Integer getProdNo() {
        return prodNo;
    }

    public void setProdNo(Integer prodNo) {
        this.prodNo = prodNo;
    }

    public ProductCategoryVO getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategoryVO productCategory) {
        this.productCategory = productCategory;
    }

    public CarCategoryVO getCarCategory() {
        return carCategory;
    }

    public void setCarCategory(CarCategoryVO carCategory) {
        this.carCategory = carCategory;
    }

    public SellerVO getSeller() {
        return seller;
    }

    public void setSeller(SellerVO seller) {
        this.seller = seller;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public Integer getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(Integer prodPrice) {
        this.prodPrice = prodPrice;
    }

    public String getProdStatus() {
        return prodStatus;
    }

    public void setProdStatus(String prodStatus) {
        this.prodStatus = prodStatus;
    }

    public Integer getRatingAmount() {
        return ratingAmount;
    }

    public void setRatingAmount(Integer ratingAmount) {
        this.ratingAmount = ratingAmount;
    }

    public Integer getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(Integer ratingStar) {
        this.ratingStar = ratingStar;
    }

    public List<ProductImageVO> getProductImage() {
        return productImage;
    }

    public void setProductImage(List<ProductImageVO> productImage) {
        this.productImage = productImage;
    }

    public Integer getProdQty() {
        return prodQty;
    }

    public void setProdQty(Integer prodQty) {
        this.prodQty = prodQty;
    }

    public List<OrdDetailVO> getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(List<OrdDetailVO> orderDetail) {
        this.orderDetail = orderDetail;
    }

    public List<BuyerRatingVO> getBuyerRatings() {
        return buyerRatings;
    }

    public void setBuyerRatings(List<BuyerRatingVO> buyerRatings) {
        this.buyerRatings = buyerRatings;
    }
}
