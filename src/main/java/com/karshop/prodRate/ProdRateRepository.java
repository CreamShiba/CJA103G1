package com.karshop.prodRate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdRateRepository extends JpaRepository<ProdRate, Integer>, JpaSpecificationExecutor<ProdRate> {
    // 根據訂單編號與商品編號尋找評價紀錄
    Optional<ProdRate> findByOrdNoAndProdNo(Integer ordNo, Integer prodNo);
}
