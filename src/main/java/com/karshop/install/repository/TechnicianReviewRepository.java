package com.karshop.install.repository;

import com.karshop.install.entity.TechnicianReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianReviewRepository extends JpaRepository<TechnicianReview, Integer> {

    // Force recompile check
    List<TechnicianReview> findByTechnicianTechNo(Integer techNo);

    List<TechnicianReview> findByMemberMemId(Integer memberNo);

    boolean existsByInstallOrderInstallOrderNo(Integer installOrderNo);

    java.util.Optional<TechnicianReview> findByInstallOrderInstallOrderNo(Integer installOrderNo);
}
