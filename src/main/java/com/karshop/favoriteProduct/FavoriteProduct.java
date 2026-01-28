package com.karshop.favoriteProduct;


import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;


@Data  // 自動生成所有的 getter/setter/toString/equals/hashCode
@Entity
@Table(name = "favorite_product")
@IdClass(FavoriteProduct.FavoriteProductId.class) // 使用複合主鍵
public class FavoriteProduct {

//    @Id
//    @ManyToOne // 設定多對一關聯
//    @JoinColumn(name = "prod_no", referencedColumnName = "prod_no")
//    private Product product; // 改為引用 Product 實體

    @Id
    @Column(name = "member_no")
    private Integer memberNo;

    // 複合主鍵類別
    @Data
    public static class FavoriteProductId implements Serializable {
        private Integer product;
        private Integer memberNo;
    }

}