package com.karshop.install.repository;

import com.karshop.install.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

    List<Technician> findByIsActive(Integer isActive);

    List<Technician> findByIsActiveAndRegionCodeValue(Integer isActive, Integer regionCode);

    Optional<Technician> findByMemberMemNo(Integer memberNo);
}
