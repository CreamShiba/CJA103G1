package com.karshop.install.repository;

import com.karshop.install.entity.InstallOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InstallOrderRepository extends JpaRepository<InstallOrder, Integer> {

        List<InstallOrder> findByMemberMemId(Integer memberNo);

        List<InstallOrder> findByMemberMemIdOrderByInstallOrderNoDesc(Integer memberNo);

        List<InstallOrder> findByMemberMemIdAndOrderStatusValueOrderByInstallOrderNoDesc(Integer memberNo,
                        Integer orderStatusValue);

        List<InstallOrder> findByTechnicianTechNoAndOrderStatusValue(Integer techNo, Integer orderStatusValue);

        List<InstallOrder> findByOrderStatusValue(Integer orderStatusValue);

        List<InstallOrder> findByPayoutStatusValue(Integer payoutStatusValue);

        List<InstallOrder> findByTechnicianTechNoAndOrderStatusValueAndPayoutStatusValue(Integer techNo,
                        Integer orderStatusValue, Integer payoutStatusValue);

        List<InstallOrder> findByOrderStatusValueAndPayoutStatusValue(Integer orderStatusValue,
                        Integer payoutStatusValue);

        @Query("SELECT o FROM InstallOrder o JOIN FETCH o.technician JOIN FETCH o.member " +
                        "WHERE o.orderStatusValue = :orderStatusValue AND o.payoutStatusValue = :payoutStatusValue " +
                        "ORDER BY o.installOrderNo DESC")
        List<InstallOrder> findPendingPayoutsWithDetails(
                        @Param("orderStatusValue") Integer orderStatusValue,
                        @Param("payoutStatusValue") Integer payoutStatusValue);

        @Query("SELECT o FROM InstallOrder o JOIN FETCH o.technician JOIN FETCH o.member " +
                        "WHERE o.orderStatusValue = :orderStatusValue AND o.payoutStatusValue = :payoutStatusValue " +
                        "ORDER BY o.installOrderNo DESC")
        List<InstallOrder> findCompletedPayoutsWithDetails(
                        @Param("orderStatusValue") Integer orderStatusValue,
                        @Param("payoutStatusValue") Integer payoutStatusValue);

        @Query("SELECT o FROM InstallOrder o WHERE o.technician.techNo = :techNo " +
                        "AND o.appointDate = :appointDate AND o.appointTimeValue = :appointTimeValue " +
                        "AND o.orderStatusValue NOT IN (4)")
        List<InstallOrder> findTechnicianActiveOrdersAtTime(
                        @Param("techNo") Integer techNo,
                        @Param("appointDate") LocalDate appointDate,
                        @Param("appointTimeValue") Integer appointTimeValue);

        @Query("SELECT o FROM InstallOrder o JOIN FETCH o.technician JOIN FETCH o.location WHERE o.installOrderNo = :id")
        java.util.Optional<InstallOrder> findByIdWithDetails(@Param("id") Integer id);
}
