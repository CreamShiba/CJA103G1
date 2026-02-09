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

    @org.springframework.beans.factory.annotation.Value("${project.init-images:false}")
    private boolean shouldInit;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (shouldInit) {
            System.out.println("【系統訊息】開始執行圖片初始化...");

            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:static/images/*.*");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName == null) continue;

                try {
                    // 🔥 修改重點 1: 解析檔名邏輯
                    // 假設檔名是 "2001_1.jpg" 或 "2001.jpg"

                    // A. 先去掉副檔名 (變成 "2001_1" 或 "2001")
                    String nameWithoutExt = fileName;
                    if (fileName.contains(".")) {
                        nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                    }

                    // B. 透過底線切割，只拿第一部分 (變成 "2001")
                    String[] parts = nameWithoutExt.split("_");
                    String prodNoStr = parts[0];

                    // C. 轉成數字
                    Integer prodNo = Integer.parseInt(prodNoStr);

                    if (prodNo >= 2001 && prodNo <= 2030) {

                        // 3. 封裝並存入
                        ProductImageVO piVO = new ProductImageVO();
                        ProductVO productVO = new ProductVO();
                        productVO.setProdNo(prodNo);

                        piVO.setProduct(productVO);
                        piVO.setUpFile(resource.getContentAsByteArray());
                        piVO.setUploadDate(LocalDate.now());

                        productImageService.addImages(piVO);

                        // 🔥 顯示更詳細的 Log，確認是哪張圖
                        System.out.println("【初始化成功】商品: " + prodNo + " | 檔案: " + fileName);

                        // 清除 Redis 快取
                        String redisKey = "product:main:" + prodNo;
                        imageRedisTemplate.delete(redisKey);
                    }
                } catch (NumberFormatException e) {
                    // 如果檔名第一段不是數字 (例如 "test_image.jpg") 就跳過
                    System.out.println("【略過檔案】無法解析商品編號: " + fileName);
                    continue;
                }
            }
            System.out.println("【系統訊息】圖片初始化完成。");
        } else {
            System.out.println("【系統訊息】目前設定為不執行圖片初始化 (shouldInit = false)。");
        }
    }
}