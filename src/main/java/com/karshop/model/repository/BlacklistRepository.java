package com.karshop.model.repository;

import com.karshop.model.entity.Blacklist;
import com.karshop.model.entity.BlacklistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BlacklistRepository extends JpaRepository<Blacklist, BlacklistId> {

    // 1. 檢查是否存在
    boolean existsByUserIdAndBlockedUserId(Integer userId, Integer blockedUserId);

    // 2. 刪除封鎖紀錄
    @Transactional // 🔴 刪除操作一定要加這行
    void deleteByUserIdAndBlockedUserId(Integer userId, Integer blockedUserId);
}