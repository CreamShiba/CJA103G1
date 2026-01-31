package com.karshop.orderProd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/orders")
public class OrderProdController {

    @Autowired
    private OrderProdService orderProdService;

    @GetMapping("/{memberNo}")
    public ResponseEntity<List<OrderProd>> getOrders(@PathVariable Integer memberNo) {
        List<OrderProd> orders = orderProdService.getMemberOrders(memberNo);
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{ordNo}/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Integer ordNo) {
        boolean success = orderProdService.completeOrder(ordNo);

        if (success) {
            return ResponseEntity.ok("訂單已完成");
        } else {
            return ResponseEntity.badRequest().body("無法更新訂單狀態，可能訂單不存在或尚未發貨");
        }
    }

    @GetMapping("/detail/{ordNo}")
    public ResponseEntity<OrderProd> getOrderDetail(@PathVariable Integer ordNo) {
        OrderProd order = orderProdService.getOneOrder(ordNo);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}