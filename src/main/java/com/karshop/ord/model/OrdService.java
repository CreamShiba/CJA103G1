package com.karshop.ord.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<OrdVO> searchOrdersForSeller(Integer sellerNo, String keyword) {
        if(keyword != null && !keyword.trim().isEmpty()) {
            return ordRepository.searchOrdersForSeller(sellerNo, keyword);
        }else{
            return ordRepository.findBySellerNoOrderByOrdDateDesc(sellerNo);
        }
    }

}
