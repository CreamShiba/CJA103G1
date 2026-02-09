package com.karshop.cart;

import com.karshop.memberCoupon.MemberCoupon;
import com.karshop.memberCoupon.MemberCouponService;
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

    @Autowired
    private MemberCouponService memberCouponService;

    // ===================================================================
    // 1. 購物車基本列表與操作
    // ===================================================================

    @GetMapping("/list")
    public String showCart(HttpSession session, Model model) {
        Integer memberNo = getMemberNo(session);
        if (memberNo == null)
            return "redirect:/members/login";

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

        if (prodNos == null || prodNos.isEmpty())
            return "redirect:/cart/list";

        Integer memberNo = member.getMemNo();
        List<cart> allItems = service.getCartByMember(memberNo);

        List<Integer> targetIds = Arrays.stream(prodNos.split(","))
                .map(String::trim).map(Integer::parseInt).toList();

        List<cart> checkoutItems = allItems.stream()
                .filter(item -> targetIds.contains(item.getProd_no())).toList();

        // 優惠券: 獲取該會員目前可用的優惠券清單 (供下拉選單使用)
        List<MemberCoupon> availableCoupons = memberCouponService.getAvailableCoupons(memberNo);

        if (checkoutItems.isEmpty())
            return "redirect:/cart/list";

        Map<Integer, ProductVO> productMap = service.getProductsForCart(checkoutItems);

        // ✅ 核心：計算小計並傳給前端 (解決圖片中顯示 NT$ 0 的問題)
        int subtotal = calcTotal(checkoutItems, productMap);

        model.addAttribute("cartItems", checkoutItems);
        model.addAttribute("productMap", productMap);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("prodNos", prodNos);
        model.addAttribute("member", member); // 供前端自動帶入收件人資訊
        model.addAttribute("availableCoupons", availableCoupons); // 優惠券: 傳給前端

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
            @RequestParam(required = false) Integer couponNo, // 優惠券: 接收優惠券編號
            @RequestParam(defaultValue = "0") Integer discountPrice, // 優惠券:對應 HTML 的 name="discount_price"
            HttpSession session, RedirectAttributes ra) {

        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "redirect:/members/login";

        try {
            // 先計算「小計+運費」總額，用來檢查 20% 限制
            int currentSubtotal = service.calculateCurrentSubtotal(member.getMemNo(), prodNos);

            // 呼叫 memberCouponService 進行校驗
            Integer actualDiscount = 0;
            if (couponNo != null && couponNo > 0) {
                // 這裡的 currentSubtotal 會被 membercouponservice 拿去 * 0.2 作為上限
                actualDiscount = memberCouponService.validateAndCalculateDiscount(member.getMemNo(), couponNo,
                        currentSubtotal);
            }

            // 呼叫 Service：注意參數順序需與 cartservice 的 processCheckout 一致
            // 最後一個 0 代表折扣預設，Service 會根據 coupon_title 再去 couponRepository 抓正確金額
            service.processCheckout(member.getMemNo(), prodNos, paymentMethod, deliveryMethod,
                    receiverName, receiverPhone, receiverAddress, couponNo, actualDiscount, member);

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
    public ResponseEntity<Map<String, Object>> updateQuantity(@RequestParam Integer prodNo,
                                                              @RequestParam Integer quantity, HttpSession session) {
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

    @PostMapping("/checkout-with-ecpay")
    @ResponseBody
    public String checkoutWithEcpay(
            @RequestParam String prodNos,
            @RequestParam("ord_payment_method") String paymentMethod,
            @RequestParam("ord_recipient") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("ord_address") String receiverAddress,
            @RequestParam("ord_ship_method") String deliveryMethod,
            @RequestParam(required = false) Integer couponNo,
            @RequestParam(defaultValue = "0") Integer discountPrice,
            HttpSession session) {

        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null)
            return "請先登入";

        try {
            // 1. 必須先計算金額，後面金流才拿得到數據
            int subtotal = service.calculateCurrentSubtotal(member.getMemNo(), prodNos);

            // 1.5 重算折扣金額 (避免前端篡改或沒傳)
            Integer actualDiscount = 0;
            if (couponNo != null && couponNo > 0) {
                actualDiscount = memberCouponService.validateAndCalculateDiscount(member.getMemNo(), couponNo,
                        subtotal);
            }

            // 2. 建立訂單 (請確保參數順序與你的 Service 一致)
            service.processCheckout(member.getMemNo(), prodNos, paymentMethod, deliveryMethod,
                    receiverName, receiverPhone, receiverAddress, couponNo, actualDiscount, member);

            // 3. 根據付款方式決定回傳內容
            if ("貨到付款".equals(paymentMethod) || "轉帳".equals(paymentMethod)) {
                // 貨到付款與轉帳不走綠界，直接跳轉到訂單查詢頁面
                String msg = "貨到付款".equals(paymentMethod) ? "訂單已成功建立 (貨到付款)" : "訂單已建立，請於 24 小時內完成轉帳";
                return "<script>alert('" + msg + "'); location.href='/pages/my-orders';</script>";
            } else {
                // 信用卡：計算總額並產生綠界表單
                int ship = "宅配".equals(deliveryMethod) ? 100 : ("超取".equals(deliveryMethod) ? 60 : 0);
                int finalAmount = subtotal + ship - actualDiscount;

                if (finalAmount < 0)
                    finalAmount = 0;

                // 呼叫產製綠界自動提交表單的方法
                return service.genEcpayForm("ORD", finalAmount, "Credit");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "結帳失敗：" + e.getMessage();
        }
    }

    // 在 cartcontroller.java 中新增

    /**
     * 綠界付款結果回調
     */
    @PostMapping("/ecpay-callback")
    @ResponseBody
    public String ecpayCallback(@RequestParam Map<String, String> params) {
        System.out.println(">>> 收到綠界回報: " + params); // 建議加上這行，測試時才看得到東西
        try {
            // 1. 驗證綠界回傳的檢查碼 (CheckMacValue)
            // String checkMacValue = params.get("CheckMacValue");
            // if (!service.verifyEcpayCallback(params, checkMacValue)) {
            //     return "0|CheckMacValue驗證失敗";
            // }

            // 2. 取得付款結果
            String rtnCode = params.get("RtnCode"); // 1 表示成功
            String merchantTradeNo = params.get("MerchantTradeNo"); // 您的訂單編號

            if ("1".equals(rtnCode)) {
                // 這裡的 1 如果代表成功，則更新為字串 "已付款"
                service.updateOrderPaymentStatus(merchantTradeNo, "已付款");
                return "1|OK";
            } else {
                // 付款失敗
                return "0|付款失敗";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "0|System Error";
        }
    }

    /**
     * 綠界付款完成後的前端返回頁面
     */
    @GetMapping("/ecpay-return")
    public String ecpayReturn(@RequestParam Map<String, String> params,
                              RedirectAttributes ra) {
        String rtnCode = params.get("RtnCode");

        if ("1".equals(rtnCode)) {
            ra.addFlashAttribute("success", "🎉 付款成功！");
        } else {
            ra.addFlashAttribute("error", "❌ 付款失敗，請重新嘗試");
        }

        return "redirect:/pages/my-orders";
    }

    // 🛠️ 手動模擬 API：瀏覽器輸入 http://localhost:8080/cart/test-pay-success?orderNo=15
    @GetMapping("/test-pay-success")
    @ResponseBody
    public String testPaySuccess(@RequestParam String orderNo) {
        try {
            // 直接呼叫 Service 更新狀態
            service.updateOrderPaymentStatus(orderNo, "已付款");
            return "✅ 測試成功！訂單 " + orderNo + " 狀態已手動更新為 [已付款]。";
        } catch (Exception e) {
            return "❌ 測試失敗：" + e.getMessage();
        }
    }
}