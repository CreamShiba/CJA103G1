package com.karshop.members.model;


import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;



public interface MembersRepository extends JpaRepository<MembersVO, Integer>,JpaSpecificationExecutor<MembersVO> {

  boolean existsByMemAcc(String memAcc);

  Optional<MembersVO> findByMemAcc(String memAcc);

  boolean existsByMemEmail(String memEmail);

  Optional<MembersVO> findByMemEmail(String memEmail);

  @Modifying
  @Transactional
  @Query(
          value = "UPDATE member SET member_login_errcount = member_login_errcount + 1, member_login_errtime = :now WHERE member_no = :id",
          nativeQuery = true
  )
  int incrementLoginError(@Param("id") Integer memNo,
                          @Param("now") Timestamp now);

  @Modifying
  @Transactional
  @Query(value="UPDATE member SET member_login_errcount = 0 WHERE member_no = :memNo", nativeQuery=true)
  int resetLoginErrorNative(@Param("memNo") Integer memNo);

}