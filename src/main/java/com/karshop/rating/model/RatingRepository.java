package com.karshop.rating.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<RatingVO, Integer> {
}
