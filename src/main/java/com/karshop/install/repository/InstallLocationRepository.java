package com.karshop.install.repository;

import com.karshop.install.entity.InstallLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallLocationRepository extends JpaRepository<InstallLocation, Integer> {

    List<InstallLocation> findByLocationStatus(Integer locationStatus);
}
