package com.karshop.model.repository;

import com.karshop.model.entity.Blacklist;
import com.karshop.model.entity.BlacklistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, BlacklistId> {

    // 1. 檢查是否存在封鎖紀錄 (JPQL 會自動解析名稱)
    boolean existsByUserIdAndBlockedUserId(Integer userId, Integer blockedUserId);

    // 2. 刪除封鎖紀錄
    @Transactional
    void deleteByUserIdAndBlockedUserId(Integer userId, Integer blockedUserId);
}