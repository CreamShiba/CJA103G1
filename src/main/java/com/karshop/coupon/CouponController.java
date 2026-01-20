package com.karshop.coupon;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    // ========== 整合版主頁面 ==========
    /**
     * 進入優惠券管理系統主頁
     * 預設顯示優惠券列表
     */
    @GetMapping("/admin")
    public String adminPage(Model model) {
        // 取得所有優惠券用於列表顯示
        List<Coupon> coupons = couponService.getAll();
        model.addAttribute("coupons", coupons);

        // 新增時的空物件
        model.addAttribute("coupon", new Coupon());

        return "coupon/adminCoupon"; // 新整合版主頁面
    }

    // ========== 列表相關 ==========
    /**
     * 取得所有優惠券 (用於重新整理列表)
     */
    @GetMapping("/list")
    public String listPage(Model model) {
        List<Coupon> coupons = couponService.getAll();
        model.addAttribute("coupons", coupons);
        return "coupon/adminCoupon";
    }

    // ========== 查詢相關 ==========
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

    // ========== 新增相關 ==========
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

        // 1. 檢查後端校驗結果
        if (result.hasErrors()) {
            return "coupon/adminCoupon";
        }

        // 2. 邏輯驗證：結束時間不可早於開始時間
        if (coupon.getCouponEnd() != null && coupon.getCouponStart() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                model.addAttribute("errorMessage", "結束時間不可早於開始時間");
                return "coupon/adminCoupon";
            }
        }

        // 新增時確保狀態為 1 (有效)
        coupon.setCouponStatus(1);
        couponService.insert(coupon);

        return "redirect:/coupon/admin"; // 重導至主頁面
    }

    // ========== 修改相關 ==========
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
                         BindingResult result) {

        if (result.hasErrors()) {
            return "coupon/updateCoupon";
        }

        // 邏輯驗證
        if (coupon.getCouponEnd() != null && coupon.getCouponStart() != null) {
            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
                return "coupon/updateCoupon";
            }
        }

        couponService.update(coupon);
        return "redirect:/coupon/admin";
    }

    // ========== 刪除相關 ==========
    /**
     * 刪除優惠券
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("couponNo") Integer couponNo) {
        couponService.delete(couponNo);
        return "redirect:/coupon/admin";
    }

//    @GetMapping("/select-page")
//    public String selectPage() {
//        return "coupon/selectCoupon"; // 對應 templates/coupon/selectCoupon.html
//    }
//
//    @PostMapping("/listByCompositeQuery")
//    public String listByCompositeQuery(
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String content,
//
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
//            Model model) {
//
//        LocalDateTime startDateTime = startDate != null ? startDate.atTime(0, 0) : null;
//        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
//
//        // 呼叫 Service 的複合查詢方法
//        List<Coupon> list = couponService.findByCompositeQuery(title, content, startDateTime, endDateTime);
//
//        // 將查詢結果傳遞給 Model，供 HTML 頁面使用
//        model.addAttribute("coupons", list);
//
//        // 導向 listOneCoupon.html
//        return "coupon/listOneCoupon";
//    }
//
//
//
//
//
//   @GetMapping("/list-page")
//   public String listPage(Model model) {
//       model.addAttribute("coupons", couponService.getAll());
//       return "coupon/listAllCoupon"; //檔案在 templates/coupon/listAllCoupon.html
//   }
//
//    // 取得單一優惠券：用於修改頁面填充資料
//    @GetMapping("/getOne/{couponNo}")
//    @ResponseBody
//    public Coupon getOne(@PathVariable("couponNo") Integer couponNo) {
//
//       return couponService.getOne(couponNo);
//    }
//
//    // 新增頁面
//    @GetMapping("/add-page")
//    public String addPage(Model model) {
//        model.addAttribute("coupon", new Coupon()); // 提供一個空的物件給頁面
//        return "coupon/addCoupon";
//    }
//
//    // 新增優惠券
//    @PostMapping("/insert")
//    public String insert(@Valid @ModelAttribute("coupon") Coupon coupon,
//                         BindingResult result,
//                         Model model) {
//
//        // 取得所有管理員姓名列表 (這裡建議傳入整體的 Admin 物件，或是單純的編號 List)
////        List<Admin> admins = adminService.getAll(); // 獲取完整物件
////        model.addAttribute("adminList", admins);
//
//        // 範例：手動建立一些測試編號，實際開發請從 Service 撈取資料
////        List<Integer> adminNoList = List.of(1, 2);
////        model.addAttribute("adminNoList", adminNoList);
//
//        // 1. 檢查後端校驗結果 (如長度限制、空白檢查)
//        if (result.hasErrors()) {
//           return "coupon/addCoupon"; // 如果有錯，返回新增頁面並顯示錯誤訊息
//        }
//
//        // 2. 額外的邏輯檢查：結束時間不可早於開始時間
//        if (coupon.getCouponEnd() != null && coupon.getCouponStart() != null) {
//            if (coupon.getCouponEnd().isBefore(coupon.getCouponStart())) {
//                model.addAttribute("errorMessage", "結束時間不可早於開始時間");
//                return "coupon/addCoupon";
//            }
//        }
//
//        couponService.insert(coupon);
//        return "redirect:/coupon/list-page";
//    }
//
//    // 更新優惠券
//    @GetMapping("/update-page/{couponNo}")
//    public String updatePage(@PathVariable("couponNo") Integer couponNo, Model model) {
//        Coupon coupon = couponService.getOne(couponNo);
//        model.addAttribute("coupon", coupon); // 將原有資料傳給修改頁面
//        return "coupon/updateCoupon"; //  templates/coupon/updateCoupon.html
//    }
//
//    @PostMapping("/update")
//    public String update(Coupon coupon) {
//        couponService.update(coupon);
//        return "redirect:/coupon/list-page"; // 更新後回到列表頁
//    }
//
//    // 刪除優惠券 用 @RequestParam 接收表單
//    @PostMapping("/delete")
//    public String delete(@RequestParam("couponNo") Integer couponNo) {
//        couponService.delete(couponNo);
//        return "redirect:/coupon/list-page"; // 刪除後回到列表頁
//    }
      
    
}
