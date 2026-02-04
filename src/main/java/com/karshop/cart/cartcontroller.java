package com.karshop.cart;

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

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/members/carts")
public class cartcontroller {

    @Autowired
    private cartservice service;

    @Autowired
    private OrdService ordService;

    @Autowired
    private com.karshop.coupon.CouponService couponService;

    // ===================================================================
    // 並發鎖 — 不動 ProductService 的情況下防止超賣
    // ===================================================================
    // 把「庫存檢查」和「建訂單」鎖在一起，同一時刻只允許一個 request 進入。
    // 第二個人必須等第一個人完成後才進來檢查庫存，
    // 這時候庫存已經被第一個人消耗掉了，不夠就會被擋住。
    //
    // ⚠️  這是類別級別的鎖，所有商品共用同一張。
    //     學校專案流量不會到那個程度，這個最單純、最不容易出錯。
    //     將來流量變大可以升級成 per-product 鎖或 Redis 分佈式鎖。
    // ===================================================================
    private final Object orderLock = new Object();

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

    @PostMapping("/cart/add")
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
    @PostMapping("/validate-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String couponCode) {

        Map<String, Object> response = new HashMap<>();
        String upperCode = couponCode.trim().toUpperCase();

        // ── 先從 CouponService 查詢（用 couponTitle 當索引碼） ──
        Integer dbDiscount = lookupCouponDiscount(upperCode);

        if (dbDiscount != null) {
            response.put("valid", true);
            response.put("discountAmount", dbDiscount);
            response.put("message", "折價券套用成功！節省 NT$ " + dbDiscount);
        } else {
            response.put("valid", false);
            response.put("message", "無效的折價券代碼");
        }

        return ResponseEntity.ok(response);
    }

    // ===== ✅ 付款處理：連動建立 Order =====
    @Transactional  // ✅ 保證 OrdVO + OrdDetailVO 同時成功或同時回滾
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

            // ===================================================================
            // ✅ synchronized(orderLock) 進入後才讀取庫存、才下單
            //    同一時刻只允許一個 request 進入這個段落。
            //    第一個人：讀庫存 → 下單 → 離開鎖
            //    第二個人：才進來讀庫存 → 此時庫存已被第一個人消耗 → 如果不夠就被擋住
            // ===================================================================
            synchronized (orderLock) {

                // ── 鎖進來後「再」從 DB 讀取最新商品資料 ──
                //    必須在鎖裡面讀，才能看到別人剛才下單後的庫存變化
                Map<Integer, ProductVO> productMap = service.getProductsForCart(cartItems);

                // ✅ 庫存檢查
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

                // ── 後端再次驗證 coupon 折扣金額（不信任前端傳來的 discountAmount） ──
                int serverDiscount = 0;
                if (couponCode != null && !couponCode.trim().isEmpty()) {
                    Integer verified = lookupCouponDiscount(couponCode.trim().toUpperCase());
                    if (verified != null) {
                        serverDiscount = verified;
                    }
                }

                // ── 計算總金額（全部用後端數據） ──
                int subtotal = calcTotal(cartItems, productMap);
                int shippingFee = 100;
                if ("store".equals(deliveryMethod)) shippingFee = 60;
                else if ("self".equals(deliveryMethod)) shippingFee = 0;

                int totalAmount = subtotal + shippingFee - serverDiscount;
                if (totalAmount < 0) totalAmount = 0;

                // ===================================================================
                // 連動 Order 資料庫：建立 OrdVO + OrdDetailVO
                // ===================================================================
//
//                MemberVO memberVO = (MemberVO) session.getAttribute("member");
//                ProductVO firstProduct = productMap.values().iterator().next();
//                ====

                if (productMap.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "❌ 購物車商品異常");
                    return "redirect:/members/carts";
                }
                ProductVO firstProduct = productMap.values().iterator().next();

                // 2. 從 Session 拿會員物件 (改用正確的邏輯與變數名稱)
                Object sessionMember = session.getAttribute("member");
                System.out.println("DEBUG: Session 中的 member 物件是: " + sessionMember);

                if (sessionMember == null) {
                    redirectAttributes.addFlashAttribute("error", "❌ 找不到會員登入資訊，請重新登入");
                    return "redirect:/members/carts";
                }

                MemberVO memberVO = (MemberVO) sessionMember; // 這裡只宣告一次 memberVO

//                ====CLEAR=====
                // ─── 建立 OrdVO ───
                OrdVO ordVO = new OrdVO();
                ordVO.setSeller(firstProduct.getSeller());
                if (memberVO != null) {
                    ordVO.setMember(memberVO);
                }
                ordVO.setCouponNo(null);                             // coupon 先 null
                ordVO.setOrdDate(LocalDateTime.now());
                ordVO.setOriginPrice(subtotal);                      // 原價（折扣前小計）
                ordVO.setDiscountPrice(serverDiscount);              // 折扣金額（後端驗證過的）
                ordVO.setOrdPrice(totalAmount);                      // 實付金額
                ordVO.setOrdStatus("待出貨");
                ordVO.setOrdPaymentStatus("待付款");
                ordVO.setOrdPaymentMethod(paymentMethod);            // credit / atm / convenience / cash
                ordVO.setOrdShipMethod(deliveryMethod);              // home / store / self
                ordVO.setOrdShipNo(null);
                ordVO.setOrdRecipient(receiverName);
                // 地址拼接（電話 + 備註，OrdVO 只有一個地址欄位）
                StringBuilder addrBuilder = new StringBuilder();
                if (receiverAddress != null) addrBuilder.append(receiverAddress);
                if (receiverPhone != null && !receiverPhone.isEmpty()) {
                    addrBuilder.append(" (電話：").append(receiverPhone).append(")");
                }
                if (orderNote != null && !orderNote.isEmpty()) {
                    addrBuilder.append(" 備註：").append(orderNote);
                }
                ordVO.setOrdAddress(addrBuilder.toString());
                ordVO.setOrdCompletedDate(null);
                ordVO.setCancelReason(null);
                ordVO.setPayoutStatus("待撥款");

                // ─── 儲存 OrdVO（拿到生成的 ordNo） ───
                ordService.addOrd(ordVO);
                System.out.println("✅ [訂單] 已建立，ordNo = " + ordVO.getOrdNo());

                // ─── 建立 OrdDetailVO（每個購物車項目一筆） ───
                List<OrdDetailVO> detailList = new ArrayList<>();
                for (cart item : cartItems) {
                    ProductVO product = productMap.get(item.getProd_no());

                    OrdDetailVO detail = new OrdDetailVO();
                    detail.setOrder(ordVO);
                    detail.setProduct(product);
                    detail.setQuantity(item.getQuantity());
                    detail.setPrice(product.getProdPrice());

                    detailList.add(detail);
                }

                // cascade=ALL → setOrderDetail 後 save 就會自動儲存細節
                ordVO.setOrderDetail(detailList);
                ordService.updateOrd(ordVO);
                System.out.println("✅ [訂單細節] 已建立，共 " + detailList.size() + " 筆");

                // ─── 清空購物車 ───
                service.clearCart(memberNo);

                redirectAttributes.addFlashAttribute("success",
                        "✅ 訂單成功！訂單編號：" + ordVO.getOrdNo() + "，總金額 NT$ " + String.format("%,d", totalAmount));

                return "redirect:/members/carts";

            } // end synchronized

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

    /**
     * 後端唯一的折價券查詢入口。
     * 優先從 CouponService 用 couponTitle 查詢（有效券 status=1）；
     * 找不到才 fallback 到暫時的測試用 hardcode map。
     * 後來 Coupon 表裡加了 coupon_code 獨立欄位時，從這裡改查詢就好。
     *
     * @return 折扣金額（找不到傳回 null）
     */
    private Integer lookupCouponDiscount(String upperCode) {
        // ── 從資料庫查詢：用 couponTitle 當索引碼 ──
        try {
            com.karshop.coupon.Coupon coupon = couponService.getAll().stream()
                    .filter(c -> c.getCouponTitle() != null
                            && c.getCouponTitle().trim().toUpperCase().equals(upperCode))
                    .findFirst()
                    .orElse(null);
            if (coupon != null) {
                return coupon.getDiscountValue();
            }
        } catch (Exception e) {
            System.err.println("⚠️  [Coupon查詢] 資料庫查詢失敗，走 fallback: " + e.getMessage());
        }

        // ── Fallback：測試用的暫時硬寫 ──
        // 正式上線後刪掉這個 map
        Map<String, Integer> fallbackCoupons = new HashMap<>();
        fallbackCoupons.put("SAVE100", 100);
        fallbackCoupons.put("SAVE500", 500);
        fallbackCoupons.put("VIP20",   200);
        return fallbackCoupons.get(upperCode);  // 找不到傳 null
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