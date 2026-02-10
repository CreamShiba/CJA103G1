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

    // 抓取 ID 列表 (用於前台論壇按鈕變色)
    @Query("SELECT f.forumPost.postId FROM PostFavorite f WHERE f.member.memNo = :memNo")
    List<Integer> findPostIdsByMemberNo(@Param("memNo") Integer memNo);

    // 🟢 關鍵新增：抓取完整物件列表 (用於個人中心收藏頁面)
    List<PostFavorite> findByMember_MemNo(Integer memNo);

    boolean existsByMember_MemNoAndForumPost_PostId(Integer memNo, Integer postId);

    @Transactional
    void deleteByMember_MemNoAndForumPost_PostId(Integer memNo, Integer postId);
}