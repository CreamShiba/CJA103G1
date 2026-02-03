package com.karshop.model.repository;

import com.karshop.model.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<ForumComment, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ForumComment c WHERE c.forumPost.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);

    // 如果未來需要根據會員刪留言，記得也要用 member.memNo
}