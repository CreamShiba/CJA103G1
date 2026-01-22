package com.karshop.rating.model;

import com.karshop.membertest.model.MemberVO;
import com.karshop.ord.model.OrdVO;
import com.karshop.sellertest.model.SellerVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

//  查詢賣家已評價過的訂單編號列表
    public List<Integer> getRatedOrderNoBySeller(Integer sellerNo) {
        return ratingRepository.findRatedOrderNoBySeller(sellerNo);
    }

    @Transactional
    public void addRating(Integer ordNo, Integer sellerNo, Integer memberNo, Integer score, String comment) {
        RatingVO ratingVO = new RatingVO();
        ratingVO.setRatingScore(score);
        ratingVO.setRatingComment(comment);

        OrdVO ordVO = new OrdVO();
        ordVO.setOrdNo(ordNo);
        ratingVO.setOrd(ordVO);

        SellerVO sellerVO = new SellerVO();
        sellerVO.setSellerNo(sellerNo);
        ratingVO.setSeller(sellerVO);

        MemberVO memberVO = new MemberVO();
        memberVO.setMemberNo(memberNo);
        ratingVO.setMember(memberVO);

        ratingRepository.save(ratingVO);
    }

    public List<RatingVO> findBySellerSeller(Integer sellerNo) {
        return ratingRepository.findBySellerSellerNo(sellerNo);
    }
}
