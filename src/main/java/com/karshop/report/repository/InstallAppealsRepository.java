package com.karshop.report.repository;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import org.springframework.data.jpa.repository.JpaRepository; //引用JPA
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstallAppealsRepository extends JpaRepository<InstallAppeals, Integer> {
    // 💡 根據會員編號查詢該會員所有的安裝申訴案件
    List<InstallAppeals> findByMemberNo(Integer memberNo);
}