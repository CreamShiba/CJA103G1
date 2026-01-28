package com.karshop.favoriteProduct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteProductService {

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    // 查詢所有收藏
    public List<FavoriteProduct> getFavoritesByMember(Integer memberNo) {
        return favoriteProductRepository.findByMemberNo(memberNo);
    }

    // 根據會員編號與商品編號取得單一收藏
    public FavoriteProduct getOne(Integer memberNo, Integer prodNo) {
        FavoriteProduct.FavoriteProductId id = new FavoriteProduct.FavoriteProductId();
        id.setMemberNo(memberNo);
        id.setProduct(prodNo); // 對應 IdClass 裡的屬性名稱為 product
        return favoriteProductRepository.findById(id).orElse(null);
    }

    // 刪除收藏
    public void delete(Integer memberNo, Integer prodNo) {
        FavoriteProduct.FavoriteProductId id = new FavoriteProduct.FavoriteProductId();
        id.setMemberNo(memberNo);
        id.setProduct(prodNo); // 對應 IdClass 裡的屬性名稱為 product
        favoriteProductRepository.deleteById(id);
    }

    // 新增收藏
    public void insert(FavoriteProduct favoriteProduct) {
        favoriteProductRepository.save(favoriteProduct);
    }




}