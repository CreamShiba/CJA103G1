package com.karshop.model.repository;

import com.karshop.model.entity.PostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PostFavorite f WHERE f.forumPost.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);

    // 🟢 修正：f.member.memId -> f.member.memNo
    @Query("SELECT f.forumPost.postId FROM PostFavorite f WHERE f.member.memNo = :memNo")
    List<Integer> findPostIdsByMemberNo(@Param("memNo") Integer memNo);

    // 🟢 修正：方法名稱改為 MemNo
    boolean existsByMember_MemNoAndForumPost_PostId(Integer memNo, Integer postId);

    @Transactional
        // 🟢 修正：方法名稱改為 MemNo
    void deleteByMember_MemNoAndForumPost_PostId(Integer memNo, Integer postId);
}