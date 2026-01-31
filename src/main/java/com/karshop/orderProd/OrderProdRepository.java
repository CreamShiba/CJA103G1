package com.karshop.orderProd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProdRepository extends JpaRepository<OrderProd, Integer> {

    // 根據會員編號查詢所有訂單，並依下單日期降序排列（最新訂單在前）
    List<OrderProd> findByMemberNoOrderByOrdDateDesc(Integer memberNo);
}
