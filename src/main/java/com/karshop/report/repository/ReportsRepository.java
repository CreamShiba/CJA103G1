package com.karshop.report.repository; //路徑

import com.karshop.report.model.Reports; //引入VO
import org.springframework.data.domain.Page; //分頁與排序工具
import org.springframework.data.domain.Pageable;// Pageable 是前端傳來的「訂單」，上面會寫著「我要看第幾頁」、「每一頁要幾筆資料」、「要按什麼欄位排序」。
import org.springframework.data.jpa.repository.JpaRepository; //繼承JpaRepository就可以擁有CRUD功能
import org.springframework.data.jpa.repository.Query; //自定義 SQL 查詢工具
import org.springframework.data.repository.query.Param; //引入Param,使他可生效
import java.util.List; //List 是最常用的資料容器，用來存放「一整串」相同類型的物件。當你不打算做分頁，只想把所有符合條件的資料一次抓回來時，就用 List。

// 繼承 JpaRepository 讓 Spring Boot 幫我處理 SQL
public interface ReportsRepository extends JpaRepository<Reports, Integer> {

    // 1. 透過會員編號找到該會員的所有檢舉案件
    List<Reports> findByMemberNo(Integer memberNo);

    /**
     * 2. 支援分頁的狀態查詢
     * Spring Data JPA 會自動解析方法名稱，生成：
     * SELECT * FROM reports WHERE status = ? LIMIT 6 OFFSET ?
     */
    // 這行是一個可以讓我查所有狀態的動態查詢器,前後台都可以用
    // Page list都是型態,設計一頁最多顯示6筆資料,所以用Page,用list的話會把符合條件的資料都丟上來,網頁會跑不動,優化方面會沒那麼有效率
    // WHERE status = ? ,?是我丟進去的狀態 ,LIMIT 6限制6筆資料 ,OFFSET ? 跳過幾筆的意思, 若從第一頁開始 OFFSET會是0,若是第二頁的話,OFFSET會是6,從第7筆資料開始開始
    Page<Reports> findByStatus(String status, Pageable pageable);

    /**
     * 3. 支援多種狀態的分頁查詢 (用於處理 'PENDING' 與 '待處理' 的相容性)
     */
    // 當初整合時狀態未統一,這個是將待處理和未處理做整合,複選查詢
    // 複選查詢和複合查詢不同,複選查詢是針對單一欄位尋找多個可能的值,複合查詢是針對多個不同欄位組合查詢。
    // 寫了in 才可以相容當初資料庫是pending 但前台是設定未處理,多狀態查詢
    // Pageable pageable：分頁說明書，決定現在要看第幾頁、一頁顯示幾筆。
    Page<Reports> findByStatusIn(List<String> statuses, Pageable pageable);

    /**
     * 💡 新增：排除特定狀態的分頁查詢
     * 用於抓取所有「不是待處理」的結案資料
     */
    Page<Reports> findByStatusNot(String status, Pageable pageable);

    /**
     * 4. 💡 新增：為了連動組員的商品管理，根據商品名稱找編號
     * 由於 Reports 直接關聯了 Product，我們可以從這裡反查。
     * 如果你的 Product Entity 類別名稱不是 "Product"，請自行修改下方 JPQL
     */
    // 這裡是 輸入商品名稱,然後抓到商品編號,跨表查詢
    // @Query 的功能,定義自定義查詢。
    // 當 JPA 的命名規則無法滿足複雜查詢需求（如多表關聯或特定篩選）時，我們可以用它來撰寫 JPQL 或原生 SQL。」
    // @Param 的功能,用來綁定參數。
    // 將 Java 方法中傳入的參數值，對應到 @Query 語句中的命名參數（例如 :name），確保資料能正確且安全地傳入 SQL 執行。
    @Query(value = "SELECT p.prod_no FROM product p WHERE p.prod_name = :prodName LIMIT 1", nativeQuery = true)
    Integer findProdNoByProdName(@Param("prodName") String prodName);
}