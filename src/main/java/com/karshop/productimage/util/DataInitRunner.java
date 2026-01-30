package com.karshop.productimage.util;

import com.karshop.product.model.ProductVO;
import com.karshop.productimage.model.ProductImageService;
import com.karshop.productimage.model.ProductImageVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitRunner implements CommandLineRunner {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private RedisTemplate<String, byte[]> imageRedisTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    // 從 application.properties 讀取開關，預設為 false
    @org.springframework.beans.factory.annotation.Value("${project.init-images:false}")
    private boolean shouldInit;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 修正點：必須把邏輯包在 if (shouldInit) 的大括號內
        if (shouldInit) {
            System.out.println("【系統訊息】開始執行圖片初始化...");

            // 核心邏輯：讀取 static/images 下的所有檔案
            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:static/images/*.*");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName == null) {
                    continue;
                }

                // 1. 提取檔名中的數字 (例如 "2001.jpg" 變成 2001)
                String prodNoStr = fileName.replaceAll("[^0-9]", "");
                if (prodNoStr.isEmpty()) continue;

                try {
                    Integer prodNo = Integer.parseInt(prodNoStr);

                    // 2. 只處理設定的 2001 ~ 2020 範圍
                    if (prodNo >= 2001 && prodNo <= 2020) {

                        // 3. 封裝成 ProductImageVO 物件
                        ProductImageVO piVO = new ProductImageVO();
                        ProductVO productVO = new ProductVO();
                        productVO.setProdNo(prodNo);

                        piVO.setProduct(productVO); // 建立關聯
                        piVO.setUpFile(resource.getContentAsByteArray()); // 存入圖片內容
                        piVO.setUploadDate(LocalDate.now());

                        // 4. 存入資料庫
                        productImageService.addImages(piVO);
                        System.out.println("【初始化成功】商品編號: " + prodNo + " (檔案: " + fileName + ")");

                        // 清除 Redis 中的舊快取
                        // 如果該 Key 不存在，Redis 只會回傳 false 或 0，並不會報錯
                        String redisKey = "product:main:" + prodNo;
                        imageRedisTemplate.delete(redisKey);
                        System.out.println("【初始化】已清除 Redis 快取: " + redisKey);
                    }
                } catch (NumberFormatException e) {
                    // 如果檔名解析失敗則跳過該檔案
                    continue;
                }
            }
            System.out.println("【系統訊息】圖片初始化完成。");
        } else {
            System.out.println("【系統訊息】目前設定為不執行圖片初始化 (shouldInit = false)。");
        }
    }
}