package com.karshop.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REDIS_CART_PREFIX = "cart:member:";
    private static final long REDIS_EXPIRE_DAYS = 7;

    /*
     * ═══════════════════════════════════════════════════════════════
     *  基本功能
     * ═══════════════════════════════════════════════════════════════
     */

    @Transactional
    public cart addToCart(Integer member_no, Integer prod_no) {
        return addToCart(member_no, prod_no, 1);
    }

    @Transactional
    public cart addToCart(Integer member_no, Integer prod_no, Integer quantity) {
        System.out.println("🛒 [新增] 會員 " + member_no + " 加入商品 " + prod_no + " 數量: " + quantity);

        Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prod_no);

        if (existing.isPresent()) {
            cart existingCart = existing.get();
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cart saved = repository.save(existingCart);
            System.out.println("✅ [更新數量] 新數量: " + saved.getQuantity());
            updateRedisCache(member_no);
            return saved;
        }

        cart newCart = new cart();
        newCart.setMember_no(member_no);
        newCart.setProd_no(prod_no);
        newCart.setQuantity(quantity);
        newCart.setAdded_time(LocalDateTime.now());

        cart saved = repository.save(newCart);
        System.out.println("✅ [MySQL] 已儲存");

        try {
            updateRedisCache(member_no);
            System.out.println("✅ [Redis] 快取已更新");
        } catch (Exception e) {
            System.err.println("⚠️  [Redis] 更新失敗: " + e.getMessage());
        }

        return saved;
    }

    @Transactional
    public cart buyNow(Integer member_no, Integer prod_no, Integer quantity) {
        System.out.println("🚀 [立即購買] 會員 " + member_no + " 商品 " + prod_no + " 數量: " + quantity);

        Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prod_no);
        if (existing.isPresent()) {
            repository.deleteByMember_noAndProd_no(member_no, prod_no);
        }

        return addToCart(member_no, prod_no, quantity);
    }

    @Transactional
    public cart updateQuantity(Integer member_no, Integer prod_no, Integer quantity) {
        System.out.println("🔄 [更新數量] 會員 " + member_no + " 商品 " + prod_no + " → " + quantity);

        Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prod_no);

        if (existing.isPresent()) {
            cart cartItem = existing.get();
            cartItem.setQuantity(quantity);
            cart saved = repository.save(cartItem);

            updateRedisCache(member_no);
            System.out.println("✅ [更新成功]");
            return saved;
        }

        throw new RuntimeException("購物車中找不到該商品");
    }

    public List<cart> getCartByMember(Integer member_no) {
        System.out.println("🔍 [查詢] 取得會員 " + member_no + " 的購物車");

        String redisKey = REDIS_CART_PREFIX + member_no;

        try {
            String cachedJson = redisTemplate.opsForValue().get(redisKey);

            if (cachedJson != null) {
                List<cart> cachedCart = objectMapper.readValue(
                        cachedJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, cart.class)
                );
                System.out.println("✅ [Redis] 從快取取得 " + cachedCart.size() + " 項商品");
                return cachedCart;
            }
        } catch (Exception e) {
            System.err.println("⚠️  [Redis] 讀取失敗: " + e.getMessage());
        }

        System.out.println("💾 [MySQL] 從資料庫查詢...");
        List<cart> cartItems = repository.findByMember_no(member_no);
        System.out.println("✅ [MySQL] 找到 " + cartItems.size() + " 項商品");

        if (!cartItems.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(cartItems);
                redisTemplate.opsForValue().set(redisKey, json, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
                System.out.println("✅ [Redis] 已快取");
            } catch (Exception e) {
                System.err.println("⚠️  [Redis] 快取失敗: " + e.getMessage());
            }
        }

        return cartItems;
    }

    // ✅ 取得購物車對應的商品 Map
    public Map<Integer, ProductVO> getProductsForCart(List<cart> cartItems) {
        Map<Integer, ProductVO> productMap = new HashMap<>();
        for (cart item : cartItems) {
            try {
                ProductVO product = productService.getOneProduct(item.getProd_no());
                if (product != null) {
                    productMap.put(item.getProd_no(), product);
                }
            } catch (Exception e) {
                System.err.println("⚠️  查詢商品 " + item.getProd_no() + " 失敗: " + e.getMessage());
            }
        }
        return productMap;
    }

    // ✅ 單獨查詢一個商品（controller 用於庫存檢查）
    public ProductVO getOneProduct(Integer prodNo) {
        return productService.getOneProduct(prodNo);
    }

    @Transactional
    public void removeFromCart(Integer member_no, Integer prod_no) {
        System.out.println("🗑️  [刪除] 會員 " + member_no + " 移除商品 " + prod_no);
        repository.deleteByMember_noAndProd_no(member_no, prod_no);
        System.out.println("✅ [MySQL] 已刪除");
        try {
            updateRedisCache(member_no);
            System.out.println("✅ [Redis] 快取已更新");
        } catch (Exception e) {
            System.err.println("⚠️  [Redis] 更新失敗: " + e.getMessage());
        }
    }

    @Transactional
    public void clearCart(Integer member_no) {
        System.out.println("🧹 [清空] 會員 " + member_no);
        repository.deleteByMember_no(member_no);
        System.out.println("✅ [MySQL] 已清空");

        String redisKey = REDIS_CART_PREFIX + member_no;
        try {
            redisTemplate.delete(redisKey);
            System.out.println("✅ [Redis] 快取已刪除");
        } catch (Exception e) {
            System.err.println("⚠️  [Redis] 刪除失敗: " + e.getMessage());
        }
    }

    @Transactional
    public int syncFromLocalStorage(Integer member_no, List<Integer> localCartItems) {
        System.out.println("🔄 [同步] 會員 " + member_no);
        int syncCount = 0;

        for (Integer prodNo : localCartItems) {
            try {
                Optional<cart> existing = repository.findByMember_noAndProd_no(member_no, prodNo);
                if (existing.isEmpty()) {
                    cart newCart = new cart();
                    newCart.setMember_no(member_no);
                    newCart.setProd_no(prodNo);
                    newCart.setQuantity(1);
                    newCart.setAdded_time(LocalDateTime.now());
                    repository.save(newCart);
                    syncCount++;
                }
            } catch (Exception e) {
                System.err.println("❌ 同步商品 " + prodNo + " 失敗");
            }
        }

        updateRedisCache(member_no);
        return syncCount;
    }

    public long getCartCount(Integer member_no) {
        return repository.countByMember_no(member_no);
    }

    public boolean isInCart(Integer member_no, Integer prod_no) {
        return repository.findByMember_noAndProd_no(member_no, prod_no).isPresent();
    }

    private void updateRedisCache(Integer member_no) {
        try {
            String redisKey = REDIS_CART_PREFIX + member_no;
            List<cart> latestCart = repository.findByMember_no(member_no);
            if (latestCart.isEmpty()) {
                redisTemplate.delete(redisKey);
            } else {
                String json = objectMapper.writeValueAsString(latestCart);
                redisTemplate.opsForValue().set(redisKey, json, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            System.err.println("⚠️  Redis 更新失敗: " + e.getMessage());
        }
    }

    public Map<String, Object> getRedisStatus(Integer member_no) {
        Map<String, Object> status = new HashMap<>();
        String redisKey = REDIS_CART_PREFIX + member_no;
        try {
            Boolean exists = redisTemplate.hasKey(redisKey);
            status.put("exists", exists);
            if (Boolean.TRUE.equals(exists)) {
                String cachedJson = redisTemplate.opsForValue().get(redisKey);
                if (cachedJson != null) {
                    List<cart> cachedCart = objectMapper.readValue(
                            cachedJson,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, cart.class)
                    );
                    status.put("itemCount", cachedCart.size());
                }
                Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                status.put("ttl_seconds", ttl);
            }
        } catch (Exception e) {
            status.put("error", e.getMessage());
        }
        return status;
    }
}