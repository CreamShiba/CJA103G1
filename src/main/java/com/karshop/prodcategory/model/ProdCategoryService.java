package com.karshop.prodcategory.model;

import com.karshop.prodcategory.model.ProdCategoryRepository;
import com.karshop.prodcategory.model.ProdCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdCategoryService {

  // 2. 依賴注入 Repository
  private final ProdCategoryRepository repository;

  @Autowired // 使用建構子注入 (推薦做法)
  public ProdCategoryService(ProdCategoryRepository repository) {
    this.repository = repository;
  }

  /**
   * 新增車種類別
   */
  @Transactional // 3. 確保交易完整性 (要嘛全成功，要嘛全失敗)
  public ProdCategoryVO addProdCategory(ProdCategoryVO prodCategory) {
    // 在這裡可以寫額外的商業邏輯檢查
    // 例如：檢查車名是否已經重複 (雖然資料庫可能有 Unique Key，但這裡先檢查更友善)
    // if (repository.existsByProdName(prodCategory.getProdName())) { ... }

    return repository.save(prodCategory);
  }

  /**
   * 修改車種類別
   */
  @Transactional
  public ProdCategoryVO updateProdCategory(Integer id, ProdCategoryVO newProdData) {
    // 先確認資料是否存在
    Optional<ProdCategoryVO> optionalProd = repository.findById(id);

    if (optionalProd.isPresent()) {
      ProdCategoryVO existingProd = optionalProd.get();

      // 更新欄位 (這裡可以決定哪些欄位允許被修改)
      existingProd.setProductCategoryName(newProdData.getProductCategoryName());

      // save 方法在有 ID 的情況下會執行 update
      return repository.save(existingProd);
    } else {
      // 實務上通常會拋出自定義 Exception
      return null;
    }
  }

  /**
   * 刪除車種類別
   */
  @Transactional
  public void deleteProdCategory(Integer id) {
    if (repository.existsById(id)) {
      repository.deleteById(id);
    } else {
      // 處理找不到 ID 的情況
    }
  }

  /**
   * 查詢單筆 (根據 ID)
   */
  public ProdCategoryVO getOneProdCategory(Integer id) {
    // 使用 Optional 避免 NullPointerException
    return repository.findById(id).orElse(null);
  }

  /**
   * 查詢全部
   */
  public List<ProdCategoryVO> getAllProdCategories() {
    return repository.findAll();
  }
}
