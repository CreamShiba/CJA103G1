package com.karshop.admins.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<AdminVO, Integer> {

  Optional<AdminVO> findByAdminAcc(String adminAcc);

  boolean existsByAdminAcc(String adminAcc);


  // 名稱模糊查詢
  List<AdminVO> findByAdminNameContaining(String adminName);

  //查所有管理員
  List<AdminVO> findAll();



}