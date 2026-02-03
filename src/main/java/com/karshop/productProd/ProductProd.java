package com.karshop.productProd;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class ProductProd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_no")
    private Integer prodNo;

    @Column(name = "product_category_no")
    private Integer productCategoryNo;

    @Column(name = "seller_no")
    private Integer sellerNo;

    @Column(name = "prod_name")
    private String productName;

    @Column(name = "prod_desc")
    private String productDesc;

    @Column(name = "prod_price")
    private Integer prodPrice;

    @Column(name = "prod_status")
    private String prodStatus;

    @Column(name = "rating_amount")
    private Integer ratingAmount;

    @Column(name = "rating_star")
    private Integer ratingStar;

}
