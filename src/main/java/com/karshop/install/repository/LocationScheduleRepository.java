package com.karshop.install.repository;

import com.karshop.install.entity.LocationSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationScheduleRepository extends JpaRepository<LocationSchedule, Integer> {

        Optional<LocationSchedule> findByLocationLocationNoAndAppointDateAndAppointTimeValue(
                        Integer locationNo, LocalDate appointDate, Integer appointTimeValue);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @org.springframework.data.jpa.repository.Query("SELECT ls FROM LocationSchedule ls WHERE ls.location.locationNo = :locationNo AND ls.appointDate = :appointDate AND ls.appointTimeValue = :appointTimeValue")
        Optional<LocationSchedule> findAndLock(
                        @org.springframework.data.repository.query.Param("locationNo") Integer locationNo,
                        @org.springframework.data.repository.query.Param("appointDate") LocalDate appointDate,
                        @org.springframework.data.repository.query.Param("appointTimeValue") Integer appointTimeValue);

        List<LocationSchedule> findByLocationLocationNoAndIsBooked(Integer locationNo, Integer isBooked);

        List<LocationSchedule> findByLocationLocationNoAndAppointDateBetween(
                        Integer locationNo, LocalDate startDate, LocalDate endDate);
}
