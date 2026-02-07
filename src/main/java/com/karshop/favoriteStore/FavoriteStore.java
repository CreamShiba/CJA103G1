package com.karshop.favoriteStore;

import com.karshop.seller_info.SellerInfo;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "favorite_store")
@IdClass(FavoriteStore.FavoriteStoreId.class) // 指定複合主鍵類別
public class FavoriteStore {

    @Id
    @Column(name = "seller_no")
    private Integer sellerNo;

    @Id
    @Column(name = "member_no")
    private Integer memberNo;

    // 在 FavoriteStore.java 中加入關聯，這樣在 list 頁面才能直接顯示店名
    @ManyToOne
    @JoinColumn(name = "seller_no", insertable = false, updatable = false)
    private SellerInfo seller;

    /**
     * 複合主鍵類別
     */
    @Data
    public static class FavoriteStoreId implements Serializable {
        private Integer sellerNo;
        private Integer memberNo;
    }


}
