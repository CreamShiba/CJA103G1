package com.karshop.coupon;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;



@Controller
@RequestMapping("/coupon")
public class CouponController {
    
    @Autowired
    private CouponService couponService;


    @GetMapping("/admin")
    public String adminPage(@RequestParam(value = "p", defaultValue = "0") Integer page, Model model) {
        int pageSize = 3; // 設定每頁顯示 3 筆
        Page<Coupon> couponPage = couponService.getAllPaged(page, pageSize);

        model.addAttribute("couponPage", couponPage);
        model.addAttribute("coupons", couponPage.getContent()); // 傳送目前的資料列表
        model.addAttribute("coupon", new Coupon()); // 用於新增表單

        return "coupon/adminCoupon";
    }

    /**
     * 取得所有優惠券
     */
    @GetMapping("/list")
    public String listPage(Model model) {
        List<Coupon> coupons = couponService.getAll();
        model.addAttribute("coupons", coupons);
        return "coupon/adminCoupon";
    }

    /**
     * 複合查詢優惠券
     */
    @PostMapping("/listByCompositeQuery")
    public String listByCompositeQuery(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            Model model) {

        LocalDateTime startDateTime = start != null ? start.atTime(0, 0) : null;
        LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;

        // 呼叫 Service 的複合查詢方法
        List<Coupon> list = couponService.findByCompositeQuery(title, content, startDateTime, endDateTime);

        model.addAttribute("coupons", list);
        model.addAttribute("coupon", new Coupon());

        // 指定側邊欄的主選單分類為優惠券
        model.addAttribute("activePage", "coupon");

        // 指定子選單的高亮項目為「查詢」
        model.addAttribute("activeTab", "query");

        // 確保分頁物件存在，避免 adminCoupon.html 的 th:if="${couponPage.totalPages > 1}" 噴錯
        // 如果查詢結果不分頁，建議給一個空的 Page 物件或在 HTML 加入 null 檢查
        model.addAttribute("couponPage", org.springframework.data.domain.Page.empty());
        model.addAttribute("queryResults", true);
        // ------------------

        return "coupon/adminCoupon";
    }

    /**
     * 顯示新增表單
     */
    @GetMapping("/add-page")
    public String addPage(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "coupon/addCoupon"; // 仍保留獨立頁面供快速新增
    }

    /**
     * 新增優惠券
     */
    @PostMapping("/insert")
    public String insert(
            @Valid @ModelAttribute("coupon") Coupon coupon,
            BindingResult result,
            Model model,
            @RequestParam(value = "p", defaultValue = "0") Integer page) {

        // 檢查校驗結果
        if (result.hasErrors()) {
            // 必須補回列表資料，否則表格會變空白
            int pageSize = 3;
            Page<Coupon> couponPage = couponService.getAllPaged(page, pageSize);
            model.addAttribute("couponPage", couponPage);
            model.addAttribute("coupons", couponPage.getContent());

            model.addAttribute("showAddSection", true);
            return "coupon/adminCoupon";
        }

        if (coupon.getCouponEnd() != null && coupon.getCouponStart() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                // 將錯誤掛在特定欄位上，Thymeleaf 才能用 th:errors 顯示
                result.rejectValue("couponEnd", "error.date", "結束時間不可早於開始時間");

                int pageSize = 3;
                Page<Coupon> couponPage = couponService.getAllPaged(page, pageSize);
                model.addAttribute("couponPage", couponPage);
                model.addAttribute("coupons", couponPage.getContent());
                model.addAttribute("showAddSection", true);
                return "coupon/adminCoupon";
            }
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getCouponStart() != null) {
            if (coupon.getCouponStart().isAfter(now)) {
                // 情況 A：開始時間在未來 -> 設為 0 (未啟用)，等待排程器
                coupon.setCouponStatus(0);
            } else if (coupon.getCouponEnd() != null && coupon.getCouponEnd().isBefore(now)) {
                // 情況 B：結束時間已過 -> 設為 2 (已過期) 或 0
                coupon.setCouponStatus(2);
            } else {
                // 情況 C：現在就在活動時間內 -> 設為 1 (有效)
                coupon.setCouponStatus(1);
            }
        }

        // 新增成功
//        coupon.setCouponStatus(0);
        couponService.insert(coupon);
        return "redirect:/coupon/admin";
    }

    /**
     * 取得單一優惠券 (用於修改表單預填)
     */
    @GetMapping("/getOne/{couponNo}")
    @ResponseBody
    public Coupon getOne(@PathVariable("couponNo") Integer couponNo) {
        return couponService.getOne(couponNo);
    }

    /**
     * 顯示修改頁面
     */
    @GetMapping("/update-page/{couponNo}")
    public String updatePage(@PathVariable("couponNo") Integer couponNo, Model model) {
        Coupon coupon = couponService.getOne(couponNo);
        model.addAttribute("coupon", coupon);
        return "coupon/updateCoupon"; // 保留獨立修改頁面
    }

    /**
     * 執行修改
     */
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("coupon") Coupon coupon,
                         BindingResult result,
                         Model model) { // 加上 Model 以防萬一需要補資料

        // 邏輯驗證：結束時間不可早於開始時間
        if (coupon.getCouponStart() != null && coupon.getCouponEnd() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                // 將自定義錯誤加入 BindingResult，對應 couponEnd 欄位
                result.rejectValue("couponEnd", "error.couponEnd", "結束時間不可早於開始時間");
            }
        }

        if (coupon.getCouponStart() != null) {
            if (coupon.getCouponStart().isAfter(LocalDateTime.now())) {
                coupon.setCouponStatus(0);
            } else {
                coupon.setCouponStatus(1);
            }
        }

        // 檢查所有錯誤（包含 @Valid 產生的與自定義的）
        if (result.hasErrors()) {
            // 不需要重新 getOne，因為 @ModelAttribute 會把使用者剛輸入的資料帶回頁面
            return "coupon/updateCoupon";
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getCouponStart() != null) {
            if (coupon.getCouponStart().isAfter(now)) {
                // 情況 A：開始時間在未來 -> 設為 0 (未啟用)，等待排程器
                coupon.setCouponStatus(0);
            } else if (coupon.getCouponEnd() != null && coupon.getCouponEnd().isBefore(now)) {
                // 情況 B：結束時間已過 -> 設為 2 (已過期) 或 0
                coupon.setCouponStatus(0);
            } else {
                // 情況 C：現在就在活動時間內 -> 設為 1 (有效)
                coupon.setCouponStatus(1);
            }
        }


        couponService.update(coupon);
        return "redirect:/coupon/admin";
    }

    /**
     * 刪除優惠券
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("couponNo") Integer couponNo) {
        couponService.delete(couponNo);
        return "redirect:/coupon/admin";
    }

}
