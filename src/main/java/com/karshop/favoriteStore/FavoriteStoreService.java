package com.karshop.favoriteStore;


import com.karshop.favoriteProduct.FavoriteProduct;
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

    // 新增收藏賣家
    public void insert(FavoriteStore favoriteStore) {
        favoriteStoreRepository.save(favoriteStore);
    }

    // 檢查是否已收藏 (回傳 boolean)
    public boolean isExists(Integer memberNo, Integer sellerNo) {
        FavoriteStore.FavoriteStoreId id = new FavoriteStore.FavoriteStoreId();
        id.setMemberNo(memberNo);
        id.setSellerNo(sellerNo);
        return favoriteStoreRepository.existsById(id);
    }

    public void delete(Integer memberNo, Integer sellerNo) {
        FavoriteStore.FavoriteStoreId id = new FavoriteStore.FavoriteStoreId();
        id.setMemberNo(memberNo);
        id.setSellerNo(sellerNo); // 對應 IdClass 裡的屬性名稱為 product
        favoriteStoreRepository.deleteById(id);
    }
}
