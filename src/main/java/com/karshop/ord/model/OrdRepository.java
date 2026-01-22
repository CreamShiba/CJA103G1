package com.karshop.ord.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdRepository extends JpaRepository<OrdVO, Integer> {

    List<OrdVO> findBySellerSellerNoOrderByOrdDateDesc(Integer sellerNo);

//    @Query("SELECT o FROM OrdVO o WHERE o.sellerNo = :sellerNo AND " +
//            "(CAST(o.ordNo AS string) LIKE %:keyword% OR o.ordRecipient LIKE %:keyword%) " +
//            "ORDER BY o.ordDate DESC")
//    List<OrdVO> searchOrdersForSeller(@Param("sellerNo") Integer sellerNo, @Param("keyword") String keyword);

    @Query("SELECT o FROM OrdVO o WHERE o.seller.sellerNo = :sellerNo " +
            "AND (:keyword IS NULL OR CAST(o.ordNo AS string) LIKE %:keyword% OR o.ordRecipient LIKE %:keyword%) " +
            "AND (:ordStatus IS NULL OR o.ordStatus = :ordStatus) " +
            "AND (:startDate IS NULL OR o.ordDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.ordDate <= :endDate) " +
            "ORDER BY o.ordDate DESC")
    List<OrdVO> compositeQuery(@Param("sellerNo") Integer sellerNo,
                               @Param("keyword") String keyword,
                               @Param("ordStatus") String ordStatus,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);
}
