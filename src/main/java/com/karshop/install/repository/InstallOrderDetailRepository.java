package com.karshop.install.repository;

import com.karshop.install.entity.InstallOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallOrderDetailRepository extends JpaRepository<InstallOrderDetail, Integer> {

    List<InstallOrderDetail> findByInstallOrderInstallOrderNo(Integer installOrderNo);
}
