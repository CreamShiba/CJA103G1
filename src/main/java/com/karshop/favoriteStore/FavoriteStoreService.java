package com.karshop.favoriteStore;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteStoreService {

    @Autowired
    private FavoriteStoreRepository favoriteStoreRepository;

    // 取得特定會員的所有收藏賣家清單
    public List<FavoriteStore> getFavoritesByMember(Integer memberNo) {
        return favoriteStoreRepository.findByMemberNo(memberNo);
    }

    // 根據會員編號與商品編號取得單一收藏
    public FavoriteStore getOne(Integer memberNo, Integer sellerNo) {
        FavoriteStore.FavoriteStoreId id = new FavoriteStore.FavoriteStoreId();
        id.setMemberNo(memberNo);
        id.setSellerNo(sellerNo);
        return favoriteStoreRepository.findById(id).orElse(null);
    }

    // 新增收藏賣家
    public void addFavorite(Integer memberNo, Integer sellerNo) {
        FavoriteStore fav = new FavoriteStore();
        fav.setMemberNo(memberNo);
        fav.setSellerNo(sellerNo);
        favoriteStoreRepository.save(fav);
    }

    // 刪除收藏賣家
    public void deleteFavorite(Integer memberNo, Integer sellerNo) {
        FavoriteStore.FavoriteStoreId id = new FavoriteStore.FavoriteStoreId();
        id.setMemberNo(memberNo);
        id.setSellerNo(sellerNo);
        favoriteStoreRepository.deleteById(id);
    }

    // 檢查是否已收藏 (回傳 boolean)
    public boolean isExists(Integer memberNo, Integer sellerNo) {
        FavoriteStore.FavoriteStoreId id = new FavoriteStore.FavoriteStoreId();
        id.setMemberNo(memberNo);
        id.setSellerNo(sellerNo);
        return favoriteStoreRepository.existsById(id);
    }
}
