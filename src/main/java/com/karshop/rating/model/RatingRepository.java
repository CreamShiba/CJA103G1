package com.karshop.rating.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<RatingVO, Integer> {

    @Query("SELECT r.ord.ordNo FROM RatingVO r WHERE r.seller.sellerNo = :sellerNo")
    List<Integer> findRatedOrderNoBySeller(@Param("sellerNo") Integer sellerNo);

    List<RatingVO> findBySellerSellerNo(Integer sellerNo);

}
