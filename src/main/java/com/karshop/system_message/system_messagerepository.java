package com.karshop.system_message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * SystemMessage 資料庫操作介面
 * 繼承 JpaRepository 後自動擁有基本的 CRUD 功能
 */
@Repository
public interface system_messagerepository extends JpaRepository<system_message, Integer> {

    /**
     * 查詢特定會員的所有通知，按時間降序排列
     */
    @Query("SELECT s FROM system_message  s WHERE s.member_no = :memberNo ORDER BY s.message_time DESC")
    List<system_message> findByMember_noOrderByMessage_timeDesc(@Param("member_no") Integer member_no);

    /**
     * 查詢特定會員的未讀通知
     */
    @Query("SELECT s FROM system_message s WHERE s.member_no = :memberNo AND s.message_status = false ORDER BY s.message_time DESC")
    List<system_message> findUnreadMessagesByMember_no(@Param("member_no") Integer member_no);

    /**
     * 計算特定會員的未讀通知數量
     */
    @Query("SELECT COUNT(s) FROM system_message s WHERE s.member_no = :memberNo AND s.message_status = false")
    Long countUnreadMessagesByMember_no(@Param("member_no") Integer member_no);

    /**
     * 查詢所有通知，按時間降序排列（後台用）
     * @return 所有系統通知
     */
    @Query("SELECT s FROM system_message s ORDER BY s.message_time DESC")
    List<system_message> findAllOrderByMessage_timeDesc();

    /**
     * 查詢特定管理員發送的所有通知
     */
    @Query("SELECT s FROM system_message s WHERE s.adm_no = :admNo ORDER BY s.message_time DESC")
    List<system_message> findByAdmNoOrderByMessage_timeDesc(@Param("adm_no") Integer adm_no);
}