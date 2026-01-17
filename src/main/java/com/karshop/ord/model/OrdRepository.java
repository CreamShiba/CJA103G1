package com.karshop.ord.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdRepository extends JpaRepository<OrdVO, Integer> {

    List<OrdVO> findBySellerNoOrderByOrdDateDesc(Integer sellerNo);

    @Query("SELECT o FROM OrdVO o WHERE o.sellerNo = :sellerNo AND " +
            "(CAST(o.ordNo AS string) LIKE %:keyword% OR o.ordRecipient LIKE %:keyword%) " +
            "ORDER BY o.ordDate DESC")
    List<OrdVO> searchOrdersForSeller(@Param("sellerNo") Integer sellerNo, @Param("keyword") String keyword);
}
