package com.karshop.model.repository;

import com.karshop.model.entity.ForumReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ForumReportRepository extends JpaRepository<ForumReport, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ForumReport r WHERE r.forumPost.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);
}