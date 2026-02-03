package com.karshop.model.repository;

import com.karshop.model.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {
    // 預設 findAll() 就夠用了，但若想按日期排序可加這行
    List<ForumPost> findAllByOrderByPostDateDesc();
}