package com.karshop.cart;

import com.karshop.members.model.MembersVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/cart")
public class cartcontroller {

    @Autowired
    private cartservice service;

    // ===================================================================
    // 1. 購物車基本列表與操作
    // ===================================================================

    @GetMapping("/list")
    public String showCart(HttpSession session, Model model) {
        Integer memberNo = getMemberNo(session);
        if (memberNo == null) return "redirect:/members/login";

        List<cart> cartItems = service.getCartByMember(memberNo);
        Map<Integer, ProductVO> productMap = service.getProductsForCart(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("productMap", productMap);
        model.addAttribute("itemCount", cartItems.size());
        model.addAttribute("totalAmount", calcTotal(cartItems, productMap));

        return "front-end/cart-list";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer prodNo,
                            @RequestParam(name = "qty", defaultValue = "1") Integer qty,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer memberNo = getMemberNo(session);

        if (memberNo == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入後再開始購物！");
            return "redirect:/members/login";
        }

        try {
            service.addToCart(memberNo, prodNo, qty);
            redirectAttributes.addFlashAttribute("success", "✅ 已成功加入購物車");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ 加入失敗: " + e.getMessage());
        }
        return "redirect:/cart/list";
    }

    // ===================================================================
    // 2. 結帳頁面 (解決圖片中 $0 與 內容顯示問題)
    // ===================================================================

    @GetMapping("/checkout")
    public String checkout(@RequestParam(name = "prodNos", required = false) String prodNos,
                           HttpSession session,
                           Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");

        if (member == null) {
            String targetUrl = "/cart/checkout?prodNos=" + prodNos;
            return "redirect:/members/login?callback=" + targetUrl;
        }

        if (prodNos == null || prodNos.isEmpty()) return "redirect:/cart/list";

        Integer memberNo = member.getMemNo();
        List<cart> allItems = service.getCartByMember(memberNo);

        List<Integer> targetIds = Arrays.stream(prodNos.split(","))
                .map(String::trim).map(Integer::parseInt).toList();

        List<cart> checkoutItems = allItems.stream()
                .filter(item -> targetIds.contains(item.getProd_no())).toList();

        if (checkoutItems.isEmpty()) return "redirect:/cart/list";

        Map<Integer, ProductVO> productMap = service.getProductsForCart(checkoutItems);

        // ✅ 核心：計算小計並傳給前端 (解決圖片中顯示 NT$ 0 的問題)
        int subtotal = calcTotal(checkoutItems, productMap);

        model.addAttribute("cartItems", checkoutItems);
        model.addAttribute("productMap", productMap);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("prodNos", prodNos);
        model.addAttribute("member", member); // 供前端自動帶入收件人資訊

        return "front-end/checkout";
    }

    // ===================================================================
    // 3. 付款處理 (與 Service 對接)
    // ===================================================================

    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam String prodNos,
            @RequestParam("ord_payment_method") String paymentMethod,
            @RequestParam("ord_recipient") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone, // ⚡ 新增：接收手機號碼
            @RequestParam("ord_address") String receiverAddress,
            @RequestParam("ord_ship_method") String deliveryMethod,
            @RequestParam(required = false) String coupon_title, // ⚡ 接收代碼字串
            HttpSession session, RedirectAttributes ra) {

        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) return "redirect:/members/login";

        try {
            // 呼叫 Service：注意參數順序需與 cartservice 的 processCheckout 一致
            // 最後一個 0 代表折扣預設，Service 會根據 coupon_title 再去 couponRepository 抓正確金額
            service.processCheckout(member.getMemNo(), prodNos, paymentMethod, deliveryMethod,
                    receiverName, receiverPhone, receiverAddress, coupon_title, 0, member);

            ra.addFlashAttribute("success", "🎉 訂單已成功建立！");
            return "redirect:/cart/list";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ 結帳失敗：" + e.getMessage());
            return "redirect:/cart/checkout?prodNos=" + prodNos;
        }
    }

    // ===================================================================
    // 4. 輔助方法
    // ===================================================================

    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(@RequestParam Integer prodNo, @RequestParam Integer quantity, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.updateQuantity(getMemberNo(session), prodNo, quantity);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Integer prodNo, HttpSession session) {
        service.removeFromCart(getMemberNo(session), prodNo);
        return "redirect:/cart/list";
    }

    private int calcTotal(List<cart> cartItems, Map<Integer, ProductVO> productMap) {
        return cartItems.stream()
                .mapToInt(item -> {
                    ProductVO p = productMap.get(item.getProd_no());
                    return (p != null) ? p.getProdPrice() * item.getQuantity() : 0;
                }).sum();
    }

    private Integer getMemberNo(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        return (member != null) ? member.getMemNo() : null;
    }
}