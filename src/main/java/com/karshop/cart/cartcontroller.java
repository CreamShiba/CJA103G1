package com.karshop.cart;

import com.karshop.members.model.MembersVO;
import com.karshop.membertest.model.MemberVO;
import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import com.karshop.orddetail.model.OrdDetailVO;
import com.karshop.product.model.ProductVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/members/carts")
public class cartcontroller {

    @Autowired
    private cartservice service;

    @Autowired
    private OrdService ordService;
    @GetMapping
    public String showCart(HttpSession session, Model model) {
        Integer memberNo = getMemberNo(session);

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
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer memberNo = getMemberNo(session);
        try {
            service.addToCart(memberNo, prodNo, quantity);
            redirectAttributes.addFlashAttribute("success", "✅ 已加入購物車");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ 加入失敗: " + e.getMessage());
        }
        return "redirect:/members/carts";
    }

    @PostMapping("/buy-now")
    public String buyNow(@RequestParam Integer prodNo,
                         @RequestParam(defaultValue = "1") Integer quantity,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Integer memberNo = getMemberNo(session);
        try {
            service.buyNow(memberNo, prodNo, quantity);
            return "redirect:/members/carts/checkout?prodNo=" + prodNo;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ 立即購買失敗: " + e.getMessage());
            return "redirect:/members/home";
        }
    }

    // ===== 更新數量 API =====
    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam Integer prodNo,
            @RequestParam Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        try {
            Integer memberNo = getMemberNo(session);

            // ✅ 庫存檢查
            ProductVO product = service.getOneProduct(prodNo);
            if (product != null && quantity > product.getProdQty()) {
                response.put("success", false);
                response.put("message", "數量超過庫存！最多可選 " + product.getProdQty() + " 件");
                return ResponseEntity.ok(response);  // 回傳 200 但 success=false
            }

            service.updateQuantity(memberNo, prodNo, quantity);
            response.put("success", true);
            response.put("message", "數量已更新");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Integer prodNo,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Integer memberNo = getMemberNo(session);
        try {
            service.removeFromCart(memberNo, prodNo);
            redirectAttributes.addFlashAttribute("success", "✅ 商品已移除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ 移除失敗");
        }
        return "redirect:/members/carts";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer memberNo = getMemberNo(session);
        try {
            service.clearCart(memberNo);
            redirectAttributes.addFlashAttribute("success", "✅ 購物車已清空");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ 清空失敗");
        }
        return "redirect:/members/carts";
    }

    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> syncFromLocalStorage(
            @RequestBody List<Integer> localCartItems,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer memberNo = getMemberNo(session);
            int syncCount = service.syncFromLocalStorage(memberNo, localCartItems);
            response.put("success", true);
            response.put("syncCount", syncCount);
            response.put("message", "成功同步 " + syncCount + " 項商品");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== 結帳頁面：同樣傳 productMap + totalAmount =====
    @GetMapping("/checkout")
    public String checkout(@RequestParam(required = false) Integer prodNo,
                           HttpSession session,
                           Model model) {
        Integer memberNo = getMemberNo(session);
        List<cart> cartItems;

        if (prodNo != null) {
            cartItems = service.getCartByMember(memberNo).stream()
                    .filter(item -> item.getProd_no().equals(prodNo))
                    .toList();
        } else {
            cartItems = service.getCartByMember(memberNo);
        }

        if (cartItems.isEmpty()) {
            return "redirect:/members/carts";
        }

        Map<Integer, ProductVO> productMap = service.getProductsForCart(cartItems);

        // ✅ 庫存檢查：如果有商品超過庫存或已下架，傳訊息警告
        for (cart item : cartItems) {
            ProductVO product = productMap.get(item.getProd_no());
            if (product != null) {
                if (item.getQuantity() > product.getProdQty()) {
                    model.addAttribute("stockWarning", "⚠️ 有商品數量超過庫存，請先調整數量！");
                }
                if ("下架中".equals(product.getProdStatus())) {
                    model.addAttribute("stockWarning", "⚠️ 有商品已經下架，請先移除該商品！");
                }
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("productMap", productMap);
        model.addAttribute("itemCount", cartItems.size());
        model.addAttribute("totalAmount", calcTotal(cartItems, productMap));

        return "front-end/checkout";
    }

    // ===== ✅ 折價券驗證 API =====
    // NOTE: 這裡先用暫時的 hardcode 方式驗證
    //       之後如果有 CouponService 可以替換成呼叫 CouponService
    @PostMapping("/validate-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String couponCode) {

        Map<String, Object> response = new HashMap<>();

        // TODO: 之後替換成從資料庫查詢 coupon
        // 例如: CouponVO coupon = couponService.findByCode(couponCode);
        Map<String, Integer> validCoupons = new HashMap<>();
        validCoupons.put("SAVE100", 100);
        validCoupons.put("SAVE500", 500);
        validCoupons.put("VIP20",   200);

        String upperCode = couponCode.trim().toUpperCase();

        if (validCoupons.containsKey(upperCode)) {
            response.put("valid", true);
            response.put("discountAmount", validCoupons.get(upperCode));
            response.put("message", "折價券套用成功！節省 NT$ " + validCoupons.get(upperCode));
        } else {
            response.put("valid", false);
            response.put("message", "無效的折價券代碼");
        }

        return ResponseEntity.ok(response);
    }

    // ===== ✅ 付款處理：連動建立 Order =====
    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String receiverPhone,
            @RequestParam(required = false) String receiverAddress,
            @RequestParam(required = false) String deliveryMethod,
            @RequestParam(required = false) String orderNote,
            @RequestParam(required = false, defaultValue = "0") Integer discountAmount,
            @RequestParam(required = false) String couponCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer memberNo = getMemberNo(session);

        try {
            List<cart> cartItems = service.getCartByMember(memberNo);

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "❌ 購物車是空的");
                return "redirect:/members/carts";
            }

            Map<Integer, ProductVO> productMap = service.getProductsForCart(cartItems);

            // ✅ 再次庫存檢查（防止前端作弊）
            for (cart item : cartItems) {
                ProductVO product = productMap.get(item.getProd_no());
                if (product == null) {
                    redirectAttributes.addFlashAttribute("error", "❌ 商品 " + item.getProd_no() + " 不存在！");
                    return "redirect:/members/carts";
                }
                if (item.getQuantity() > product.getProdQty()) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ 「" + product.getProdName() + "」數量超過庫存（最多 " + product.getProdQty() + " 件）");
                    return "redirect:/members/carts/checkout";
                }
                if ("下架中".equals(product.getProdStatus())) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ 「" + product.getProdName() + "」已經下架，無法訂購！");
                    return "redirect:/members/carts/checkout";
                }
            }

            // 計算總金額
            int subtotal = calcTotal(cartItems, productMap);
            int shippingFee = 100;
            if ("store".equals(deliveryMethod)) shippingFee = 60;
            else if ("self".equals(deliveryMethod)) shippingFee = 0;

            int totalAmount = subtotal + shippingFee - (discountAmount != null ? discountAmount : 0);
            if (totalAmount < 0) totalAmount = 0;

            // ===================================================================
            // ✅ 連動 Order 資料庫：建立 OrdVO + OrdDetailVO
            // ===================================================================

            // 從 session 拿會員物件
            MembersVO memberVO = (MembersVO) session.getAttribute("member");

            // 從第一個商品拿賣家（同一平台，目前先用第一個商品的賣家）
            ProductVO firstProduct = productMap.values().iterator().next();

            // ─── 建立 OrdVO ───
            OrdVO ordVO = new OrdVO();
            ordVO.setSeller(firstProduct.getSeller());           // 賣家（從商品拿）
            if (memberVO != null) {
                ordVO.setMember(memberVO);                       // 會員
            }
            ordVO.setCouponNo(null);                             // coupon 還沒有資料庫，先 null
            // 之後有 CouponVO 時改為 coupon.getCouponNo()
            ordVO.setOrdDate(LocalDateTime.now());               // 訂單日期
            ordVO.setOriginPrice(subtotal);                      // 原價（小計，折扣前）
            ordVO.setDiscountPrice(discountAmount != null ? discountAmount : 0); // 折扣金額
            ordVO.setOrdPrice(totalAmount);                      // 實付金額（小計 + 運費 - 折扣）
            ordVO.setOrdStatus("待出貨");
            ordVO.setOrdPaymentStatus("待付款");
            ordVO.setOrdPaymentMethod(paymentMethod);            // credit / atm / convenience / cash
            ordVO.setOrdShipMethod(deliveryMethod);              // home / store / self
            ordVO.setOrdShipNo(null);                            // 出貨單號，出貨後才填
            ordVO.setOrdRecipient(receiverName);                 // 收件人姓名
            // 地址拼接（電話 + 備註也放進來，因為 OrdVO 只有一個地址欄位）
            StringBuilder addrBuilder = new StringBuilder();
            if (receiverAddress != null) addrBuilder.append(receiverAddress);
            if (receiverPhone != null && !receiverPhone.isEmpty()) {
                addrBuilder.append(" (電話：").append(receiverPhone).append(")");
            }
            if (orderNote != null && !orderNote.isEmpty()) {
                addrBuilder.append(" 備註：").append(orderNote);
            }
            ordVO.setOrdAddress(addrBuilder.toString());
            ordVO.setOrdCompletedDate(null);                     // 完成日期，完成後才填
            ordVO.setCancelReason(null);
            ordVO.setPayoutStatus("待撥款");

            // ─── 先儲存 OrdVO（拿到生成的 ordNo） ───
            ordService.addOrd(ordVO);
            System.out.println("✅ [訂單] 已建立，ordNo = " + ordVO.getOrdNo());

            // ─── 建立 OrdDetailVO（每個購物車項目一筆） ───
            // ⚠️  setOrder / setProduct 從 mappedBy 確認無誤
            // ⚠️  數量和單價的 setter 名稱是從 OrdDetailVO.java 推斷的，
            //     如果 compile 錯請看你們的 OrdDetailVO.java 確認正確的 setter 名稱
            List<OrdDetailVO> detailList = new ArrayList<>();
            for (cart item : cartItems) {
                ProductVO product = productMap.get(item.getProd_no());

                OrdDetailVO detail = new OrdDetailVO();
                detail.setOrder(ordVO);                          // 對應哪張訂單
                detail.setProduct(product);                      // 對應哪個商品
                detail.setQuantity(item.getQuantity());    // 購買數量  ← 如果錯請看 OrdDetailVO 調整
                detail.setPrice(product.getProdPrice()); // 單價   ← 如果錯請看 OrdDetailVO 調整

                detailList.add(detail);
            }

            // 設定細節列表，利用 OrdVO 裡的 cascade=ALL 自動儲存
            ordVO.setOrderDetail(detailList);
            ordService.updateOrd(ordVO);                         // save 觸發 cascade，細節一起存進去
            System.out.println("✅ [訂單細節] 已建立，共 " + detailList.size() + " 筆");

            // ─── 清空購物車 ───
            service.clearCart(memberNo);

            redirectAttributes.addFlashAttribute("success",
                    "✅ 訂單成功！訂單編號：" + ordVO.getOrdNo() + "，總金額 NT$ " + String.format("%,d", totalAmount));

            return "redirect:/members/carts";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ 付款失敗：" + e.getMessage());
            return "redirect:/members/carts";
        }
    }

    @GetMapping("/redis-status")
    @ResponseBody
    public Map<String, Object> getRedisStatus(HttpSession session) {
        Integer memberNo = getMemberNo(session);
        return service.getRedisStatus(memberNo);
    }

    // ===== 輔助方法 =====

    private int calcTotal(List<cart> cartItems, Map<Integer, ProductVO> productMap) {
        int total = 0;
        for (cart item : cartItems) {
            ProductVO product = productMap.get(item.getProd_no());
            if (product != null) {
                total += product.getProdPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private Integer getMemberNo(HttpSession session) {
        Integer memberNo = (Integer) session.getAttribute("member_no");
        if (memberNo == null) {
            memberNo = 1;
            session.setAttribute("member_no", memberNo);
            System.out.println("⚠️  [測試模式] 使用預設會員編號: " + memberNo);
        }
        return memberNo;
    }
}