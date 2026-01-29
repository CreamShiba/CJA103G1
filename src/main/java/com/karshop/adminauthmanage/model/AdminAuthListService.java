package com.karshop.adminauthmanage.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthListService {

  @Autowired
  private AdminAuthListRepository repository;

  // 取出所有管理功能
  public List<AdminAuthListVO> getAll() {
    return repository.findAll();
  }

  // 用 ID 找單筆功能
  public Optional<AdminAuthListVO> findById(Integer id) {
    return repository.findById(id);
  }

  // 新增
  public AdminAuthListVO create(AdminAuthListVO vo) {
    return repository.save(vo);
  }

  // 更新
  public AdminAuthListVO update(AdminAuthListVO vo) {
    return repository.save(vo);
  }

}