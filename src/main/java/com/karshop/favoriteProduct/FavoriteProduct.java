package com.karshop.favoriteProduct;


import com.karshop.productProd.ProductProd;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Entity
@Table(name = "favorite_product")
@IdClass(FavoriteProduct.FavoriteProductId.class) // 使用複合主鍵
public class FavoriteProduct {

    @Id
    @Column(name = "member_no")
    private Integer memberNo;

    @Id
    @Column(name = "prod_no")
    private Integer prodNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prod_no", referencedColumnName = "prod_no", insertable = false, updatable = false)
    private ProductProd productProd; // 保持關聯功能但設為唯讀

    // 複合主鍵類別
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteProductId implements Serializable {
        private Integer memberNo;
        private Integer prodNo;
    }

}