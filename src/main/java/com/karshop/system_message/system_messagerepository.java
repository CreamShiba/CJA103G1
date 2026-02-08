package com.karshop.system_message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface system_messagerepository extends JpaRepository<system_message, Integer> {


    /**
     * 查詢特定會員的未讀通知 (已修正)
     */
    @Query("SELECT s FROM system_message s WHERE s.member_no = :memberNo AND s.message_status = false ORDER BY s.message_time DESC")
    List<system_message> findUnreadMessagesByMember_no(@Param("memberNo") Integer memberNo);

    /**
     * 計算特定會員的未讀通知數量 (已修正)
     */
    @Query("SELECT COUNT(s) FROM system_message s WHERE s.member_no = :memberNo AND s.message_status = false")
    Long countUnreadMessagesByMember_no(@Param("memberNo") Integer memberNo);

    @Query("SELECT s FROM system_message s WHERE s.member_no = :memberNo ORDER BY s.message_time DESC")
    List<system_message> findByMember_noOrderByMessage_timeDesc(@Param("memberNo") Integer memberNo);

    /**
     * 查詢所有通知 (後台用)
     */
    @Query("SELECT s FROM system_message s ORDER BY s.message_time DESC")
    List<system_message> findAllOrderByMessage_timeDesc();

    /**
     * 查詢特定管理員發送的所有通知
     * 修正：將 @Param("adm_no") 改為 @Param("admNo") 以對齊 Query 裡的 :admNo
     */
    @Query("SELECT s FROM system_message s WHERE s.adm_no = :admNo ORDER BY s.message_time DESC")
    List<system_message> findByAdmNoOrderByMessage_timeDesc(@Param("admNo") Integer admNo);
}