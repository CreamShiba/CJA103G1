package com.karshop.install.repository;

import com.karshop.install.entity.TechnicianService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianServiceRepository extends JpaRepository<TechnicianService, Integer> {

    List<TechnicianService> findByTechnicianTechNo(Integer techNo);

    List<TechnicianService> findByTechnicianTechNoAndServiceStatus(Integer techNo, Integer serviceStatus);

    List<TechnicianService> findByServiceItemServiceNoAndServiceStatus(Integer serviceNo, Integer serviceStatus);
}
