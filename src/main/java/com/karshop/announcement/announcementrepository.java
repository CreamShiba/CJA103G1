package com.karshop.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface announcementrepository extends JpaRepository<announcement, Integer> {

    // 根據管理員編號查詢
	@Query("SELECT a FROM Announcement a WHERE a.adm_no = :admNo")
	List<announcement> findByAdm_no(@Param("admNo") Integer admNo);

    List<announcement> findByStatus(String status);

    List<announcement> findByIsnew(Boolean isnew);

    List<announcement> findByTitleContaining(String title);

    List<announcement> findByContentContaining(String content);

    // ✅ 查詢已發佈的公告，按置頂和時間排序
    // isnew DESC 會讓 true (1) 排在前面，false (0) 排在後面
    @Query("SELECT a FROM Announcement a WHERE a.status = :status ORDER BY a.isnew DESC, a.create_time DESC")
    List<announcement> findPublishedAnnouncementsOrdered(@Param("status") String status);
}