package com.karshop.seller_info;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface sellerinforepository extends JpaRepository<sellerinfo, Integer> {
    /**
     * 根據商店名稱搜尋（模糊查詢）
     * 方法名稱遵循 Spring Data JPA 命名規則，會自動產生 SQL
     */
    @Query("SELECT s FROM sellerinfo s WHERE s.shop_name LIKE CONCAT('%', :shopName, '%')")
    List<sellerinfo> findByShopName(@Param("shopName") String shopName);

    /**
     * 根據驗證狀態查詢
     */
    @Query("SELECT s FROM sellerinfo s WHERE s.isverified = :isverified")
    List<sellerinfo> findByVerified(@Param("isverified") Boolean isverified);
    /**
     * 根據狀態查詢(分頁)
     */
    @Query("SELECT s FROM sellerinfo s WHERE s.status = :status")
    Page<sellerinfo> findByStatusPage(@Param("status") String status, Pageable pageable);

    /**
     * 自訂查詢:根據評分星數排序
     */
    @Query("SELECT s FROM sellerinfo s ORDER BY s.rating_star DESC")
    List<sellerinfo> findTopRatedSellers();

    /**
     * 根據會員編號查詢賣家
     */
    @Query("SELECT s FROM sellerinfo s WHERE s.member_no = :memberNo")
    Optional<sellerinfo> findByMemberNo(@Param("memberNo") Integer memberNo);
}