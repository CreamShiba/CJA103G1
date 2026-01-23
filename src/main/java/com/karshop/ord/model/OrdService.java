package com.karshop.ord.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrdService {

    @Autowired
    private OrdRepository ordRepository;

    public void addOrd(OrdVO ord) {
        ordRepository.save(ord);
    }

    public void updateOrd(OrdVO ordVO) {
        ordRepository.save(ordVO);
    }

    public OrdVO getOneOrd(Integer ordNo) {
        return ordRepository.findById(ordNo).orElse(null);
    }

    public List<OrdVO> getAllOrd() {
        return ordRepository.findAll();
    }

    public List<OrdVO> getOrdBySeller(Integer sellerNo) {
        return ordRepository.findBySellerSellerNoOrderByOrdDateDesc(sellerNo);
    }

    //  賣家訂單的複合查詢
    public List<OrdVO> searchOrdersForSeller(Integer sellerNo, String keyword, String ordStatus, LocalDate startDate, LocalDate endDate) {
        if(keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        if(ordStatus != null && ordStatus.trim().isEmpty()) {
           ordStatus = null;
        }

//      前端傳LocalDate要改成LocalDateTime
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if(startDate != null) {
            // 變成當天的 00:00:00
            startDateTime = startDate.atStartOfDay();
        }

        if(endDate != null) {
            // 變成當天的 23:59:59
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        return ordRepository.compositeQuery(sellerNo, keyword, ordStatus, startDateTime, endDateTime);

    }

    public List<OrdVO> searchOrders(String keyword, String ordStatus, LocalDate startDate, LocalDate endDate, String payoutStatus) {
        if(keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        if(ordStatus != null && ordStatus.trim().isEmpty()) {
            ordStatus = null;
        }

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if(startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }

        if(endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        if(payoutStatus != null && payoutStatus.trim().isEmpty()) {
            payoutStatus = null;
        }

       return ordRepository.searchOrders(keyword, ordStatus, startDateTime, endDateTime, payoutStatus);
    }

    public void updatePayoutStatus(Integer ordNo, String NewStatus) {
        OrdVO ordVO = ordRepository.findById(ordNo).orElse(null);
        if(ordVO != null) {
            ordVO.setPayoutStatus(NewStatus);
            ordRepository.save(ordVO);
        }
    }



}
