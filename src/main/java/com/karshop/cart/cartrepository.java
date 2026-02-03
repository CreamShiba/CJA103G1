package com.karshop.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface cartrepository extends JpaRepository<cart, cart.cartId> {

    // 根據會員編號查詢該會員的所有購物車項目
    @Query("SELECT c FROM cart c WHERE c.member_no = :member_no ORDER BY c.added_time DESC")
    List<cart> findByMember_no(@Param("member_no") Integer member_no);

    // 查詢某會員是否已加入某商品
    @Query("SELECT c FROM cart c WHERE c.member_no = :member_no AND c.prod_no = :prod_no")
    Optional<cart> findByMember_noAndProd_no(@Param("member_no") Integer member_no,
                                             @Param("prod_no") Integer prod_no);

    // 根據商品編號查詢有哪些會員加入了這個商品
    @Query("SELECT c FROM cart c WHERE c.prod_no = :prod_no")
    List<cart> findByProd_no(@Param("prod_no") Integer prod_no);

    // 刪除某會員的某個商品
    @Modifying
    @Query("DELETE FROM cart c WHERE c.member_no = :member_no AND c.prod_no = :prod_no")
    void deleteByMember_noAndProd_no(@Param("member_no") Integer member_no,
                                     @Param("prod_no") Integer prod_no);

    // 刪除某會員的所有購物車項目
    @Modifying
    @Query("DELETE FROM cart c WHERE c.member_no = :member_no")
    void deleteByMember_no(@Param("member_no") Integer member_no);

    // 查詢某會員的購物車商品數量
    @Query("SELECT COUNT(c) FROM cart c WHERE c.member_no = :member_no")
    long countByMember_no(@Param("member_no") Integer member_no);


}