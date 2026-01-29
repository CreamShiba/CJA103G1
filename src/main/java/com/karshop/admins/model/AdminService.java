package com.karshop.admins.model;

import com.karshop.admins.model.AdminRepository;
import com.karshop.admins.model.AdminVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  @Autowired
  private AdminRepository adminRepository;


  public Optional<AdminVO> findByAdminAcc(String adminAcc) {
    return adminRepository.findByAdminAcc(adminAcc);
  }

  //依 ID 查單筆
  public AdminVO getById(Integer adminId) {
    return adminRepository.findById(adminId)
            .orElse(null);
  }

  //依名稱模糊查詢
  public List<AdminVO> findByNameLike(String adminName) {
    return adminRepository.findByAdminNameContaining(adminName);
  }

  // 取得所有管理員清單
  public List<AdminVO> getAll() {
    return adminRepository.findAll();
  }

  public Page<AdminVO> getAll(Pageable pageable) {
    // 直接呼叫 Repository 內建的分頁方法
    return adminRepository.findAll(pageable);
  }

  //查詢帳號是否存在
  public boolean existsByAdminAcc(String adminAcc) {
    return adminRepository.existsByAdminAcc(adminAcc);
  }

  // 建立新管理員
  public void create(AdminVO vo) {
    adminRepository.save(vo);
  }

  // 更新既有管理員
  public void update(AdminVO vo) {
    adminRepository.save(vo);
  }

}