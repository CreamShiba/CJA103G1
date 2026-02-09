package com.karshop.carcategory.model;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karshop.carcategory.model.CarCategoryRepository;
import com.karshop.carcategory.model.CarCategoryVO;

@Service // 1. 告訴 Spring 這是一個 Service Bean
public class CarCategoryService {

  // 2. 依賴注入 Repository
  private final CarCategoryRepository repository;

  @Autowired // 使用建構子注入 (推薦做法)
  public CarCategoryService(CarCategoryRepository repository) {
    this.repository = repository;
  }

  //模糊搜尋
  public List<CarCategoryVO> getCarCategoriesByName(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return repository.findAll();
    }
    return repository.findByCarNameContaining(keyword);
  }

  /**
   * 新增車種類別
   */
  @Transactional // 3. 確保交易完整性 (要嘛全成功，要嘛全失敗)
  public CarCategoryVO addCarCategory(CarCategoryVO carCategory) {
    // 在這裡可以寫額外的商業邏輯檢查
    // 例如：檢查車名是否已經重複 (雖然資料庫可能有 Unique Key，但這裡先檢查更友善)
    // if (repository.existsByCarName(carCategory.getCarName())) { ... }

    return repository.save(carCategory);
  }
  //檢查車種類別是否重複
  public boolean checkDuplicate(CarCategoryVO vo) {
    return repository.existsByMakeAndCarNameAndProdInterval(
            vo.getMake(),
            vo.getCarName(),
            vo.getProdInterval()
    );
  }
  /**
   * 修改車種類別
   */
  @Transactional
  public CarCategoryVO updateCarCategory(Integer id, CarCategoryVO newCarData) {
    // 先確認資料是否存在
    Optional<CarCategoryVO> optionalCar = repository.findById(id);

    if (optionalCar.isPresent()) {
      CarCategoryVO existingCar = optionalCar.get();

      // 更新欄位 (這裡可以決定哪些欄位允許被修改)
      existingCar.setCarName(newCarData.getCarName());
      existingCar.setMake(newCarData.getMake());
      existingCar.setProdInterval(newCarData.getProdInterval());

      // save 方法在有 ID 的情況下會執行 update
      return repository.save(existingCar);
    } else {
      // 實務上通常會拋出自定義 Exception
      return null;
    }
  }

  /**
   * 刪除車種類別
   */
  @Transactional
  public void deleteCarCategory(Integer id) {
    if (repository.existsById(id)) {
      repository.deleteById(id);
    } else {
      // 處理找不到 ID 的情況
    }
  }

  /**
   * 查詢單筆 (根據 ID)
   */
  public CarCategoryVO getOneCarCategory(Integer id) {
    // 使用 Optional 避免 NullPointerException
    return repository.findById(id).orElse(null);
  }

  /**
   * 查詢全部
   */
  public List<CarCategoryVO> getAllCarCategories() {
    return repository.findAll();
  }
}