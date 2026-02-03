package com.karshop.carcategory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 注意這裡不一樣
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.karshop.carcategory.model.CarCategoryVO;
import com.karshop.carcategory.model.CarCategoryService;

import jakarta.validation.Valid;

@Controller // 1. 這裡改成 @Controller，代表要回傳網頁
@RequestMapping("/admins")
public class CarCategoryController {

  private final CarCategoryService carCategoryService;

  @Autowired
  public CarCategoryController(CarCategoryService carCategoryService) {
    this.carCategoryService = carCategoryService;
  }

  /**
   * 1. 列表頁：顯示所有資料
   * 網址: GET /car-category/list
   */
  @GetMapping("/carcate/list")
  public String listAll(Model model) {
    List<CarCategoryVO> list = carCategoryService.getAllCarCategories();

    // 2. 將資料放入 Model，讓 Thymeleaf 可以在 HTML 中讀取 "carList"
    model.addAttribute("carList", list);

    // 3. 回傳 HTML 檔案名稱 (不含 .html 副檔名)
    // Spring 會去 src/main/resources/templates/ 下找 car_category_list.html
    return "back-end/carcategory/car_category_list";
  }

  /**
   * 2. 新增頁：顯示空白表單
   * 網址: GET /car-category/add
   */
  @GetMapping("/carcate/add")
  public String showAddForm(Model model) {
    // 傳入一個空的 VO 物件，讓表單可以綁定欄位
    CarCategoryVO carCategoryVO = new CarCategoryVO();
    model.addAttribute("carCategoryVO", carCategoryVO);

    return "back-end/carcategory/car_category_add"; // 對應 templates/car_category_add.html
  }

  /**
   * 3. 執行新增動作 (接收 Form 表單)
   * 網址: POST /car-category/insert
   */
  @PostMapping("/carcate/insert")
  public String insert(@Valid @ModelAttribute("carCategoryVO") CarCategoryVO carCategoryVO,
                       BindingResult result,
                       Model model) {

    // 4. 錯誤驗證處理
    // 如果 @Valid 驗證失敗 (例如名稱空白)，result 會包含錯誤訊息
    if (result.hasErrors()) {
      // 驗證失敗，直接返回原本的新增頁面，Thymeleaf 會顯示錯誤訊息
      return "back-end/carcategory/car_category_add";
    }

    // 驗證成功，呼叫 Service 存檔
    carCategoryService.addCarCategory(carCategoryVO);

    // 5. 使用 redirect 重導向到列表頁 (避免使用者按 F5 重複送出表單)
    return "redirect:/admins/carcate/list";
  }

  /**
   * 4. 修改頁：顯示帶有舊資料的表單
   * 網址: GET /car-category/edit/{id}
   */
  @GetMapping("/carcate/edit/{id}")
  public String showEditForm(@PathVariable Integer id, Model model) {
    CarCategoryVO carVO = carCategoryService.getOneCarCategory(id);

    // 把舊資料放進 Model，頁面欄位就會自動填入值
    model.addAttribute("carCategoryVO", carVO);

    return "back-end/carcategory/car_category_edit"; // 對應 templates/car_category_edit.html
  }

  /**
   * 5. 執行修改動作
   * 網址: POST /car-category/update
   */
  @PostMapping("/carcate/update")
  public String update(@Valid @ModelAttribute("carCategoryVO") CarCategoryVO carCategoryVO,
                       BindingResult result,
                       Model model) {

    if (result.hasErrors()) {
      // 驗證失敗，停留在修改頁面，並顯示錯誤
      return "back-end/carcategory/car_category_edit";
    }

    // 注意：這裡假設 Form 表單有用 hidden 欄位傳送 carCategoryNo
    carCategoryService.updateCarCategory(carCategoryVO.getCarCategoryNo(), carCategoryVO);

    return "redirect:/admins/carcate/list";
  }

  /**
   * 6. 刪除動作
   * 網址: POST /car-category/delete/{id}
   * (HTML form 預設只支援 GET/POST，所以這裡用 PostMapping)
   */
  @PostMapping("/carcate/delete/{id}")
  public String delete(@PathVariable Integer id) {
    carCategoryService.deleteCarCategory(id);
    return "redirect:/admins/carcate/list";
  }
}