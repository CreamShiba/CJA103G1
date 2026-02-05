package com.karshop.prodRate;


import com.karshop.orderProd.OrderProd;
import com.karshop.orderProd.OrderProdService;
import com.karshop.productProd.ProductImg;
import com.karshop.productProd.ProductImgService;
import com.karshop.productProd.ProductProd;
import com.karshop.productProd.ProductProdService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/prodRate")
public class ProdRateController {

    @Autowired
    private ProdRateService prodRateService;

    @Autowired
    private OrderProdService orderProdService;

    @Autowired
    private ProductProdService productProdService;

    @Autowired
    private ProductImgService productImgService;

    // 1. 從訂單頁進入填寫評價的入口
    @GetMapping("/add-page")
    public String addPage(@RequestParam("ordNo") Integer ordNo, Model model) {

        try {
            OrderProd order = orderProdService.getOneOrder(ordNo);

            // 檢查訂單是否存在
            if (order == null) {
                model.addAttribute("errorMessage", "訂單不存在");
                return "error/404";
            }

            ProdRateForm form = new ProdRateForm();
            List<ProdRate> rates = new ArrayList<>();


            Map<Integer, ProductProd> productMap = new HashMap<>();
            Map<Integer, Integer> productImgMap = new HashMap<>();
            Map<Integer, Integer> rateStatusMap = new HashMap<>();

            if (order.getOrdDetails() != null && !order.getOrdDetails().isEmpty()) {
                order.getOrdDetails().forEach(detail -> {
                    ProdRate existing = prodRateService.findByOrdAndProd(ordNo, detail.getProdNo());
                    ProdRate pr;
                    if (existing != null) {
                        // 已有評價記錄 - 使用現有記錄
                        pr = existing;
                        rateStatusMap.put(detail.getProdNo(), existing.getRateStatus());

                        if (existing.getRateStatus() == 2) {
                            System.out.println("⚠️ 評價已鎖定 - 商品編號:" + detail.getProdNo());
                        } else if (existing.getRateStatus() == 1) {
                            System.out.println("✅ 加載評價進入編輯 - 商品編號:" + detail.getProdNo());
                        }
                    } else {
                        // 新建記錄
                        pr = new ProdRate();
                        pr.setOrdNo(ordNo);
                        pr.setMemberNo(order.getMemberNo());
                        pr.setProdNo(detail.getProdNo());
                        pr.setRateStatus(null); // 新建時為 null
                        rateStatusMap.put(detail.getProdNo(), null);
                    }

                    rates.add(pr);

                    // 獲取商品資訊
                    ProductProd product = productProdService.getOneProduct(detail.getProdNo());
                    if (product != null) {
                        productMap.put(detail.getProdNo(), product);
                    }

                    // 獲取商品圖片
                    try {
                        List<ProductImg> images = productImgService.getByProdNo(detail.getProdNo());
                        if (images != null && !images.isEmpty()) {
                            Integer imgNo = images.get(0).getImgNo();
                            if (imgNo != null) {
                                productImgMap.put(detail.getProdNo(), imgNo);
                                System.out.println("✅ 商品 " + detail.getProdNo() + " 圖片編號: " + imgNo);
                            }
                        } else {
                            System.out.println("⚠️ 商品 " + detail.getProdNo() + " 無圖片");
                        }
                    } catch (Exception e) {
                        System.err.println("❌ 載入商品 " + detail.getProdNo() + " 圖片錯誤: " + e.getMessage());
                    }
                });
            } else {
                // 訂單沒有商品明細
                model.addAttribute("errorMessage", "此訂單沒有商品明細");
                return "error/error";
            }

            form.setRates(rates);

            model.addAttribute("form", form);
            model.addAttribute("productMap", productMap);
            model.addAttribute("productImgMap", productImgMap);
            model.addAttribute("rateStatusMap", rateStatusMap);

            // 呼叫封裝好的方法，這會統一處理商品資訊與 hasEditableRate
            prepareModelData(form, model);

            return "prodRate/addProdRate";

        } catch (Exception e) {
            System.err.println("❌ 加載評價頁面時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入評價頁面失敗,請稍後再試");
            return "error/error";
        }
    }

    // 2. 執行批次新增評價
    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("form") ProdRateForm form,
                         BindingResult result,
                         @RequestParam(value = "ratePicFiles", required = false) MultipartFile[] files,
                         Model model) {

        if (result.hasErrors()) {
            System.out.println("⚠️ 驗證失敗，共 " + result.getErrorCount() + " 個錯誤");
            result.getFieldErrors().forEach(f -> {
                System.out.println("  欄位 [" + f.getField() + "]：" + f.getDefaultMessage());
            });
            prepareModelData(form, model);
            return "prodRate/addProdRate";
        }

        try {
            List<ProdRate> rates = form.getRates();
            int successCount = 0;

            for (int i = 0; i < rates.size(); i++) {
                ProdRate pr = rates.get(i);

                // ✅ 檢查是否鎖定（status = 2）
                if (pr.getRateStatus() != null && pr.getRateStatus() == 2) {
                    System.err.println("❌ 此評價已鎖定，無法編輯 - 商品編號：" + pr.getProdNo());
                    model.addAttribute("dbError", "有評價記錄已鎖定，無法重複編輯");
                    prepareModelData(form, model);
                    return "prodRate/addProdRate";
                }

                // 處理上傳的圖片
                if (files != null && i < files.length && !files[i].isEmpty()) {
                    try {
                        byte[] imageBytes = files[i].getBytes();
                        if (imageBytes.length > 0) {
                            pr.setRatePic(imageBytes);
                            System.out.println("✅ 圖片 " + (i + 1) + " 上傳成功");
                        }
                    } catch (IOException e) {
                        System.err.println("❌ 圖片 " + (i + 1) + " 上傳失敗：" + e.getMessage());
                    }
                }

                pr.setRateTime(java.time.LocalDateTime.now());

                // 簡化邏輯：根據 prodRateNo 判斷操作
                if (pr.getProdRateNo() == null) {
                    // 新建評價：直接提交為狀態 1
                    prodRateService.submitRate(pr);
                    System.out.println("✅ 新評價 " + (i + 1) + "/" + rates.size() + " - 商品編號：" + pr.getProdNo());
                } else if (pr.getRateStatus() == 1) {
                    // 編輯評價：從狀態 1 改為狀態 2
                    prodRateService.updateRate(pr);
                    System.out.println("✅ 編輯評價 " + (i + 1) + "/" + rates.size() + " - 商品編號：" + pr.getProdNo());
                }

                successCount++;
            }

            System.out.println("✅ 成功保存 " + successCount + " 筆評價");
            return "redirect:/pages/my-orders";

        } catch (Exception e) {
            System.err.println("❌ 資料庫保存失敗");
            e.printStackTrace();
            prepareModelData(form, model);
            model.addAttribute("dbError", "評價提交失敗，請稍後重試");
            return "prodRate/addProdRate";
        }
    }

    /**
     * 封裝:補足頁面顯示商品名稱與圖片所需的 Map 資料
     */
    private void prepareModelData(ProdRateForm form, Model model) {
        Map<Integer, ProductProd> productMap = new HashMap<>();
        Map<Integer, Integer> productImgMap = new HashMap<>();
        Map<Integer, Integer> rateStatusMap = new HashMap<>();

        for (ProdRate pr : form.getRates()) {
            Integer prodNo = pr.getProdNo();

            // 商品名稱
            ProductProd product = productProdService.getOneProduct(prodNo);
            if (product != null) {
                productMap.put(prodNo, product);
            }

            // 商品圖片 ID
            List<ProductImg> images = productImgService.getByProdNo(prodNo);
            if (images != null && !images.isEmpty()) {
                productImgMap.put(prodNo, images.get(0).getImgNo());
            }

            // 評價狀態
            rateStatusMap.put(prodNo, pr.getRateStatus());
        }

        model.addAttribute("productMap", productMap);
        model.addAttribute("productImgMap", productImgMap);
        model.addAttribute("rateStatusMap", rateStatusMap);

        // 檢查是否有可編輯的評價記錄
        boolean hasEditableRate = false;
               if( form.getRates() != null){
                   hasEditableRate = form.getRates().stream()
                    .anyMatch(r -> r.getRateStatus() == null || r.getRateStatus() != 2);
               }
        model.addAttribute("hasEditableRate", hasEditableRate);
    }

    // 讀取評價圖片
    @GetMapping("/DBGifReader")
    @ResponseBody
    public byte[] dbGifReader(@RequestParam("prodRateNo") Integer prodRateNo) {
        ProdRate pr = prodRateService.getOne(prodRateNo);
        return (pr != null) ? pr.getRatePic() : null;
    }

    // 讀取商品圖片 (供評價頁面顯示商品樣貌用)
    @GetMapping("/productImage/DBGifReader")
    @ResponseBody
    public byte[] productImageReader(@RequestParam("imgNo") Integer imgNo) {
        ProductImg productImage = productImgService.getOneProductImage(imgNo);
        if (productImage != null && productImage.getUpFile() != null) {
            return productImage.getUpFile();
        }
        return null;
    }

    @GetMapping("/member/{memberNo}")
    @ResponseBody
    public List<ProdRate> getMemberRates(@PathVariable Integer memberNo) {
        // 直接從資料庫依據 memberNo 查詢
        List<ProdRate> filteredList = prodRateService.getByMember(memberNo);

        System.out.println("====== 評價狀態查詢 ======");
        System.out.println("查詢會員編號: " + memberNo);
        System.out.println("查得記錄筆數: " + (filteredList != null ? filteredList.size() : 0));
        return filteredList;
    }

    // 獲取所有評價記錄 (供前端查詢用)
    @GetMapping("/all")
    @ResponseBody
    public List<ProdRate> getAllRates() {
        return prodRateService.getAll();
    }

}


