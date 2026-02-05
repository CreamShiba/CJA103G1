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

    // 1. 將 :memberNo 改成 :member_no
    @Query("SELECT s FROM system_message s WHERE s.member_no = :member_no ORDER BY s.message_time DESC")
    List<system_message> findByMember_noOrderByMessage_timeDesc(@Param("member_no") Integer member_no);

    // 2. 將 :memberNo 改成 :member_no
    @Query("SELECT s FROM system_message s WHERE s.member_no = :member_no AND s.message_status = false ORDER BY s.message_time DESC")
    List<system_message> findUnreadMessagesByMember_no(@Param("member_no") Integer member_no);

    // 3. 將 :memberNo 改成 :member_no
    @Query("SELECT COUNT(s) FROM system_message s WHERE s.member_no = :member_no AND s.message_status = false")
    Long countUnreadMessagesByMember_no(@Param("member_no") Integer member_no);

    // 4. 下方的 adm_no 也請檢查（目前看起來你下方的 adm_no 是對應正確的，但建議檢查一致性）
    @Query("SELECT s FROM system_message s WHERE s.adm_no = :adm_no ORDER BY s.message_time DESC")
    List<system_message> findByAdmNoOrderByMessage_timeDesc(@Param("adm_no") Integer adm_no);
}
