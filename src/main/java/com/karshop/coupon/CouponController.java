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
     * 取得所有優惠券 (用於重新整理列表)
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
        model.addAttribute("queryResults", true); // 標記為查詢結果頁面

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
            Model model) {

        // 1. 檢查校驗結果
        if (result.hasErrors()) {
            // 發生錯誤，除了回傳列表資料，還要傳遞「顯示新增區塊」的訊號
            List<Coupon> coupons = couponService.getAll();
            model.addAttribute("coupons", coupons);
            model.addAttribute("showAddSection", true); // 關鍵：告訴前端要打開新增表單
            return "coupon/adminCoupon";
        }

        // 2. 邏輯驗證
        if (coupon.getCouponEnd() != null && coupon.getCouponStart() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                List<Coupon> coupons = couponService.getAll();
                model.addAttribute("coupons", coupons);
                model.addAttribute("errorMessage", "結束時間不可早於開始時間");
                model.addAttribute("showAddSection", true); // 關鍵訊號
                return "coupon/adminCoupon";
            }
        }

        // 新增成功
        coupon.setCouponStatus(1);
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

        // 1. 邏輯驗證：結束時間不可早於開始時間
        if (coupon.getCouponStart() != null && coupon.getCouponEnd() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                // 將自定義錯誤加入 BindingResult，對應 couponEnd 欄位
                result.rejectValue("couponEnd", "error.couponEnd", "結束時間不可早於開始時間");
            }
        }

        // 2. 檢查所有錯誤（包含 @Valid 產生的與自定義的）
        if (result.hasErrors()) {
            // 不需要重新 getOne，因為 @ModelAttribute 會把使用者剛輸入的資料帶回頁面
            return "coupon/updateCoupon";
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
