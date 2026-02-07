package com.karshop.favoriteStore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, FavoriteStore.FavoriteStoreId> {

    // 根據會員編號取得該會員收藏的所有賣家
    List<FavoriteStore> findByMemberNo(Integer memberNo);

    // 根據賣家編號取得收藏該賣家的所有會員（可用於統計熱度）
    List<FavoriteStore> findBySellerNo(Integer sellerNo);


}