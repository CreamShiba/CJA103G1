package com.karshop.sellertest.model;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

//  管理員搜尋
    public List<SellerVO> searchSellers(String keyword, String status) {

        if(keyword != null && keyword.trim().isEmpty()){
            keyword = null;
        }

        if(status != null && status.trim().isEmpty()){
            status = null;
        }

        return sellerRepository.searchSeller(keyword, status);
    }

//  更改賣家狀態
    @Transactional
    public void updateStatus(Integer sellerNo, String newStatus) {
        SellerVO sellerVO = sellerRepository.findById(sellerNo).orElse(null);
        if (sellerVO != null) {
            sellerVO.setSellerStatus(newStatus);
            sellerRepository.save(sellerVO);
        }
    }

    public SellerVO findByMemberNo(Integer memberNo) {
            return sellerRepository.findByMemberMemNo(memberNo);
    }
}
