package com.karshop.model.repository;

import com.karshop.model.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);

    // 🟢 修正：對齊參數名稱 memNo，查詢語句確保 memberNo 正確
    @Query("SELECT l.postId FROM PostLike l WHERE l.memberNo = :memNo")
    List<Integer> findLikedPostIdsByMemberNo(@Param("memNo") Integer memNo);

    // 檢查點讚狀態 (確保與 Entity 欄位名一致)
    boolean existsByMemberNoAndPostId(Integer memberNo, Integer postId);

    // 刪除特定點讚
    @Transactional
    void deleteByMemberNoAndPostId(Integer memberNo, Integer postId);
}