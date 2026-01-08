package com.karshop.admin.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

  @Autowired
  AdminRepository adminRepository;

  //新增管理員
  public void addAdmin(AdminVO adminVO) {
    adminRepository.save(adminVO);
  }
  //更新管理員
  public void updateAdmin(AdminVO adminVO) {
    adminRepository.save(adminVO);
  }
  //刪除管理員
  public void deleteAdmin(Integer admNo) {
    if (adminRepository.existsById(admNo))
      adminRepository.deleteById(admNo);
  }
  //查詢全部管理員
  public List<AdminVO> findAll() {
    return adminRepository.findAll();
  }
  //查詢單一管理員
  public AdminVO findOne(Integer admNo) {
    Optional<AdminVO> adminVO = adminRepository.findById(admNo);
    return adminVO.orElse(null);
  }
  //查詢帳號是否存在
  boolean existsByAdmAccount(String admAccount){
    return existsByAdmAccount(admAccount);
  }
}
