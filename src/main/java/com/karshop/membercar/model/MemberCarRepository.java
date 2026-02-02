package com.karshop.membercar.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MemberCarRepository extends JpaRepository<MemberCarVO, Integer> {

  // 根據會員編號查詢該會員的所有車輛
  List<MemberCarVO> findByMember_MemNo(Integer memNo);
}