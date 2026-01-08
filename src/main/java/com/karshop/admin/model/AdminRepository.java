package com.karshop.admin.model;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRepository extends JpaRepository<AdminVO, Integer> {
//  Optional<AdminVO> findByAdmAccount(String admAccount);
//  boolean existsByAdmAccount(String admAccount);
  //名稱模糊查詢
  @Query(value="from AdminVO where admName like?1")
  List<AdminVO> findAllByAdmName(String admName);

}
