package com.karshop.model.repository;

import com.karshop.model.entity.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Integer> {

    // 🟢 修正：將 senderId/receiverId 改為 senderNo/receiverNo 以對齊 Entity 屬性
    @Query("SELECT c FROM PrivateChat c WHERE " +
            "(c.senderNo = :user1 AND c.receiverNo = :user2) OR " +
            "(c.senderNo = :user2 AND c.receiverNo = :user1) " +
            "ORDER BY c.sendTime ASC")
    List<PrivateChat> findChatHistory(@Param("user1") Integer user1, @Param("user2") Integer user2);
}