package com.karshop.sellertest.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerRepository extends JpaRepository<SellerVO, Integer> {

    List<SellerVO> findBySellerStatus(String sellerStatus);

    @Query("SELECT s FROM SellerVO s where " +
            "(:keyword IS NULL OR s.sellerName LIKE %:keyword% OR s.sellerEmail LIKE %:keyword%) " +
            "AND :status IS NULL OR s.sellerStatus = :status " +
            "ORDER BY s.createTime DESC")
    List<SellerVO> searchSeller(@Param("keyword") String keyword, @Param("status") String status);

    SellerVO findByMemberMemNo(Integer memberNo);


}
