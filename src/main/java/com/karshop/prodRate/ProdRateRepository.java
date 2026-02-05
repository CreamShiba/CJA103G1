package com.karshop.prodRate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdRateRepository extends JpaRepository<ProdRate, Integer>, JpaSpecificationExecutor<ProdRate> {
    // 根據訂單編號與商品編號尋找評價紀錄
    Optional<ProdRate> findByOrdNoAndProdNo(Integer ordNo, Integer prodNo);

    // 【新增】優化查詢：根據會員編號直接從資料庫過濾評價
    List<ProdRate> findByMemberNo(Integer memberNo);

    // 【新增】若未來需要，也可以根據訂單編號獲取該訂單所有品項的評價
    List<ProdRate> findByOrdNo(Integer ordNo);
}
