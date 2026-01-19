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
        return ordRepository.findBySellerNoOrderByOrdDateDesc(sellerNo);
    }

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


}
