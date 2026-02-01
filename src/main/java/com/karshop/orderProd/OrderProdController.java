package com.karshop.orderProd;

import com.karshop.members.model.MembersVO;
import com.karshop.utils.LoginUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/orders")
public class OrderProdController {

    @Autowired
    private OrderProdService orderProdService;
    @Autowired
    private LoginUserHolder loginUserHolder;

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderProd>> getOrders() {

        MembersVO member = loginUserHolder.get();

        // 未登入回傳 401
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OrderProd> orders = orderProdService.getMemberOrders(member.getMemId());

        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{ordNo}/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Integer ordNo) {
        MembersVO member = loginUserHolder.get();
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");

        // 建議加強：在 Service 或這裡檢查該 ordNo 的 memberNo 是否等於當前登入者
        // 防止惡意使用者亂戳別人的 ordNo
        boolean success = orderProdService.completeOrder(ordNo);

        if (success) {
            return ResponseEntity.ok("訂單已完成");
        } else {
            return ResponseEntity.badRequest().body("無法更新訂單狀態，可能訂單不存在或尚未發貨");
        }
    }

    @GetMapping("/detail/{ordNo}")
    public ResponseEntity<OrderProd> getOrderDetail(@PathVariable Integer ordNo) {
        MembersVO member = loginUserHolder.get();
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderProd order = orderProdService.getOneOrder(ordNo);

        // 安全檢查：如果這張訂單不屬於當前登入會員，禁止存取
        if (order == null || !order.getMemberNo().equals(member.getMemId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(order);
    }
}