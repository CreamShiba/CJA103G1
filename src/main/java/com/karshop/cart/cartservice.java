package com.karshop.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karshop.coupon.CouponService;
import com.karshop.members.model.MembersVO;
import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import com.karshop.orddetail.model.OrdDetailVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class cartservice {

    @Autowired
    private cartrepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrdService ordService;

    @Autowired
    private CouponService couponService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object orderLock = new Object(); // 防止併發下單導致庫存錯誤

    private static final String REDIS_CART_PREFIX = "cart:member:";
    private static final long REDIS_EXPIRE_DAYS = 7;

    /*
     * ═══════════════════════════════════════════════════════════════
     * 1. 核心結帳邏輯 (處理訂單、扣庫存、同步 Redis)
     * ═══════════════════════════════════════════════════════════════
     */
    @Transactional
    public void processCheckout(Integer memberNo, String prodNos, String paymentMethod, String deliveryMethod,
                                String receiverName, String receiverPhone, String receiverAddress,
                                String couponNoStr, Integer discount, MembersVO memberVO) {

        // 使用鎖確保在扣除庫存到存入訂單期間，其他執行緒不能干擾
        synchronized (orderLock) {
            // A. 取得該會員購物車並篩選出勾選的商品
            List<cart> allItems = repository.findByMember_no(memberNo);
            List<Integer> buyIds = Arrays.stream(prodNos.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            List<cart> buyItems = allItems.stream()
                    .filter(item -> buyIds.contains(item.getProd_no()))
                    .toList();

            if (buyItems.isEmpty()) throw new RuntimeException("結帳商品無效或購物車已空");

            // B. 初始化訂單物件 (OrdVO)
            OrdVO ordVO = new OrdVO();
            ordVO.setMember(memberVO);
            ordVO.setOrdDate(LocalDateTime.now());
            ordVO.setOrdStatus("待出貨");
            ordVO.setOrdPaymentMethod(paymentMethod); // 信用卡, 轉帳, 超商代收
            ordVO.setOrdShipMethod(deliveryMethod);   // 宅配, 超取, 自取
            ordVO.setOrdRecipient(receiverName);
            // 合併電話至地址欄位以符合資料庫設計
            ordVO.setOrdAddress(receiverAddress + " (Tel: " + receiverPhone + ")");
            ordVO.setOrdPaymentStatus("信用卡".equals(paymentMethod) ? "已付款" : "未付款");
            ordVO.setPayoutStatus("未撥款");

            // C. 處理商品明細與庫存扣除
            int subtotal = 0;
            List<OrdDetailVO> detailList = new ArrayList<>();

            for (cart item : buyItems) {
                ProductVO product = productService.getOneProduct(item.getProd_no());
                if (product == null || "下架中".equals(product.getProdStatus())) {
                    throw new RuntimeException("商品 [" + (product != null ? product.getProdName() : "未知") + "] 已下架");
                }
                if (item.getQuantity() > product.getProdQty()) {
                    throw new RuntimeException("商品 [" + product.getProdName() + "] 庫存不足，剩餘：" + product.getProdQty());
                }

                // 🔥 核心操作：扣除庫存並更新
                product.setProdQty(product.getProdQty() - item.getQuantity());
                productService.updateProduct(product);

                subtotal += product.getProdPrice() * item.getQuantity();

                OrdDetailVO detail = new OrdDetailVO();
                detail.setOrder(ordVO);
                detail.setProduct(product);
                detail.setQuantity(item.getQuantity());
                detail.setPrice(product.getProdPrice());
                detailList.add(detail);
            }

            // D. 計算運費邏輯
            int shippingFee = 0;
            if ("宅配".equals(deliveryMethod)) shippingFee = 100;
            else if ("超取".equals(deliveryMethod)) shippingFee = 60;

            // E. 完善訂單金額與折價券資訊
            // 這裡抓取第一件商品的賣家作為訂單賣家 (假設購物車內為同一賣家)
            ordVO.setSeller(productService.getOneProduct(buyItems.get(0).getProd_no()).getSeller());
            ordVO.setOriginPrice(subtotal);
            ordVO.setDiscountPrice(discount != null ? discount : 0);
            ordVO.setOrdPrice(Math.max(0, subtotal + shippingFee - (discount != null ? discount : 0)));
            ordVO.setOrderDetail(detailList);

//            if (couponNoStr != null && !couponNoStr.isEmpty()) {
//                ordVO.setCoupon_no(Integer.parseInt(couponNoStr));
//            }

            // F. 存檔與清空購物車
            ordService.addOrd(ordVO);
            removeItemsFromCart(memberNo, buyIds);
        }
    }

    /*
     * ═══════════════════════════════════════════════════════════════
     * 2. 購物車基本操作 (MySQL & Redis 同步)
     * ═══════════════════════════════════════════════════════════════
     */

    @Transactional
    public cart addToCart(Integer member_no, Integer prod_no, Integer quantity) {
        Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prod_no);
        cart result;
        if (existing.isPresent()) {
            cart existingCart = existing.get();
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            result = repository.save(existingCart);
        } else {
            cart newCart = new cart();
            newCart.setMember_no(member_no);
            newCart.setProd_no(prod_no);
            newCart.setQuantity(quantity);
            newCart.setAdded_time(LocalDateTime.now());
            result = repository.save(newCart);
        }
        updateRedisCache(member_no);
        return result;
    }

    @Transactional
    public cart updateQuantity(Integer member_no, Integer prod_no, Integer quantity) {
        Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prod_no);
        if (existing.isPresent()) {
            cart cartItem = existing.get();
            cartItem.setQuantity(quantity);
            cart saved = repository.save(cartItem);
            updateRedisCache(member_no);
            return saved;
        }
        throw new RuntimeException("購物車中找不到該商品");
    }

    public List<cart> getCartByMember(Integer member_no) {
        String redisKey = REDIS_CART_PREFIX + member_no;
        try {
            String cachedJson = redisTemplate.opsForValue().get(redisKey);
            if (cachedJson != null) {
                return objectMapper.readValue(cachedJson, objectMapper.getTypeFactory().constructCollectionType(List.class, cart.class));
            }
        } catch (Exception e) { System.err.println("Redis 讀取失敗: " + e.getMessage()); }

        List<cart> cartItems = repository.findByMember_no(member_no);
        if (!cartItems.isEmpty()) updateRedisCache(member_no);
        return cartItems;
    }

    public Map<Integer, ProductVO> getProductsForCart(List<cart> cartItems) {
        Map<Integer, ProductVO> productMap = new HashMap<>();
        for (cart item : cartItems) {
            ProductVO product = productService.getOneProduct(item.getProd_no());
            if (product != null) productMap.put(item.getProd_no(), product);
        }
        return productMap;
    }

    @Transactional
    public void removeFromCart(Integer member_no, Integer prod_no) {
        repository.deleteByMember_noAndProd_no(member_no, prod_no);
        updateRedisCache(member_no);
    }

    @Transactional
    public void removeItemsFromCart(Integer memberNo, List<Integer> prodNos) {
        for (Integer prodNo : prodNos) {
            repository.deleteByMember_noAndProd_no(memberNo, prodNo);
        }
        updateRedisCache(memberNo);
    }

    /*
     * ═══════════════════════════════════════════════════════════════
     * 3. 輔助方法 (Redis 快取管理與工具)
     * ═══════════════════════════════════════════════════════════════
     */

    private void updateRedisCache(Integer member_no) {
        try {
            String redisKey = REDIS_CART_PREFIX + member_no;
            List<cart> latestCart = repository.findByMember_no(member_no);
            if (latestCart.isEmpty()) {
                redisTemplate.delete(redisKey);
            } else {
                redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(latestCart), REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
            }
        } catch (Exception e) { System.err.println("Redis 更新失敗: " + e.getMessage()); }
    }

    public long getCartCount(Integer member_no) {
        return repository.countByMember_no(member_no);
    }
}