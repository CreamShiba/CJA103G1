package com.karshop.faq;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * FAQ 資料庫操作介面
 * 繼承 JpaRepository 後自動擁有基本的 CRUD 功能
 */
@Repository
public interface faqrepository extends JpaRepository<faq, Integer> {

    /**
     * 查詢特定狀態的 FAQ，並按建立時間降序排列
     * 用於前台顯示已發布的 FAQ
     *
     * 注意：方法名稱必須與 Service 層呼叫的名稱一致
     */
    @Query(value = "SELECT * FROM faq WHERE status = ?1 ORDER BY create_date DESC", nativeQuery = true)
    List<faq> findByStatusOrderBycreate_dateDesc(String status);

    /**
     * 查詢全部 FAQ，並按建立時間降序排列
     * 用於後台顯示所有 FAQ（包含草稿）
     */
    @Query(value = "SELECT * FROM faq ORDER BY create_date DESC", nativeQuery = true)
    List<faq> findAllOrderBycreate_dateDesc();
}