package com.karshop.orderProd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderProdService {

    @Autowired
    private OrderProdRepository orderProdRepository;

    @Transactional(readOnly = true)
    public List<OrderProd> getMemberOrders(Integer memberNo) {
        // 從資料庫查詢該會員的所有訂單
        return orderProdRepository.findByMemberNoOrderByOrdDateDesc(memberNo);
    }

    // 確認收貨並更新狀態
    @Transactional
    public boolean completeOrder(Integer ordNo) {
        Optional<OrderProd> orderOpt = orderProdRepository.findById(ordNo);

        if (orderOpt.isPresent()) {
            OrderProd order = orderOpt.get();

            // 檢查是否為已發貨狀態
            if ("已出貨".equals(order.getOrdStatus())) {
                order.setOrdStatus("已完成");
                order.setOrdCompletedDate(LocalDateTime.now());
                orderProdRepository.save(order); // JPA 會執行 Update
                return true;
            }
        }
        return false;
    }

    /**
     * 根據訂單編號取得單一訂單物件
     * @param ordNo 訂單編號
     */
    public OrderProd getOneOrder(Integer ordNo) {
        // 使用 Optional 處理，如果找不到資料就回傳 null
        return orderProdRepository.findById(ordNo).orElse(null);
    }
}