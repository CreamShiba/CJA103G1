package com.karshop.install.repository;

import com.karshop.install.entity.Adm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmRepository extends JpaRepository<Adm, Integer> {

    Optional<Adm> findByAdmAccount(String admAccount);
}
