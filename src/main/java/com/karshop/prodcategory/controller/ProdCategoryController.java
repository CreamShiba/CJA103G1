package com.karshop.prodcategory.controller;

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

import com.karshop.prodcategory.model.ProdCategoryVO;
import com.karshop.prodcategory.model.ProdCategoryService;

import jakarta.validation.Valid;

@Controller // 1. 這裡改成 @Controller，代表要回傳網頁
@RequestMapping("/admins")
public class ProdCategoryController {

  private final ProdCategoryService prodCategoryService;

  @Autowired
  public ProdCategoryController(ProdCategoryService prodCategoryService) {
    this.prodCategoryService = prodCategoryService;
  }

  /**
   * 1. 列表頁：顯示所有資料
   * 網址: GET /admins/prodcate/list
   */
  @GetMapping("/prodcate/list")
  public String listAll(Model model) {
    List<ProdCategoryVO> list = prodCategoryService.getAllProdCategories();

    // 2. 將資料放入 Model，讓 Thymeleaf 可以在 HTML 中讀取 "prodList"
    model.addAttribute("prodList", list);

    // 3. 回傳 HTML 檔案名稱 (不含 .html 副檔名)
    // Spring 會去 src/main/resources/templates/ 下找 prod_category_list.html
    return "back-end/prodcategory/prod_category_list";
  }

  /**
   * 2. 新增頁：顯示空白表單
   * 網址: GET /admins/prodcate/add
   */
  @GetMapping("/prodcate/add")
  public String showAddForm(Model model) {
    // 傳入一個空的 VO 物件，讓表單可以綁定欄位
    ProdCategoryVO prodCategoryVO = new ProdCategoryVO();
    model.addAttribute("prodCategoryVO", prodCategoryVO);

    return "back-end/prodcategory/prod_category_add"; // 對應 templates/prod_category_add.html
  }

  /**
   * 3. 執行新增動作 (接收 Form 表單)
   * 網址: POST /admins/prodcate/insert
   */
  @PostMapping("/prodcate/insert")
  public String insert(@Valid @ModelAttribute("prodCategoryVO") ProdCategoryVO prodCategoryVO,
                       BindingResult result,
                       Model model) {

    // 4. 錯誤驗證處理
    // 如果 @Valid 驗證失敗 (例如名稱空白)，result 會包含錯誤訊息
    if (result.hasErrors()) {
      // 驗證失敗，直接返回原本的新增頁面，Thymeleaf 會顯示錯誤訊息
      return "back-end/prodcategory/prod_category_add";
    }

    // 驗證成功，呼叫 Service 存檔
    prodCategoryService.addProdCategory(prodCategoryVO);

    // 5. 使用 redirect 重導向到列表頁 (避免使用者按 F5 重複送出表單)
    return "redirect:/admins/prodcate/list";
  }

  /**
   * 4. 修改頁：顯示帶有舊資料的表單
   * 網址: GET /admins/prodcate/edit/{id}
   */
  @GetMapping("/prodcate/edit/{id}")
  public String showEditForm(@PathVariable Integer id, Model model) {
    ProdCategoryVO prodcateVO = prodCategoryService.getOneProdCategory(id);

    // 把舊資料放進 Model，頁面欄位就會自動填入值
    model.addAttribute("prodCategoryVO", prodcateVO);

    return "back-end/prodcategory/prod_category_edit"; // 對應 templates/prod_category_edit.html
  }

  /**
   * 5. 執行修改動作
   * 網址: POST /admins/prodcate/update
   */
  @PostMapping("/prodcate/update")
  public String update(@Valid @ModelAttribute("prodCategoryVO") ProdCategoryVO prodCategoryVO,
                       BindingResult result,
                       Model model) {

    if (result.hasErrors()) {
      // 驗證失敗，停留在修改頁面，並顯示錯誤
      return "back-end/prodcategory/prod_category_edit";
    }

    // 注意：這裡假設 Form 表單有用 hidden 欄位傳送 prodCategoryNo
    prodCategoryService.updateProdCategory(prodCategoryVO.getProductCategoryNo(), prodCategoryVO);

    return "redirect:/admins/prodcate/list";
  }

  /**
   * 6. 刪除動作
   * 網址: POST /admins/prodcate/delete/{id}
   * (HTML form 預設只支援 GET/POST，所以這裡用 PostMapping)
   */
  @PostMapping("/prodcate/delete/{id}")
  public String delete(@PathVariable Integer id) {
    prodCategoryService.deleteProdCategory(id);
    return "redirect:/admins/prodcate/list";
  }
}