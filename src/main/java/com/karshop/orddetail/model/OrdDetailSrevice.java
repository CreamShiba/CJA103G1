package com.karshop.orddetail.model;

import com.karshop.ord.model.OrdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdDetailSrevice {

    @Autowired
    private OrdDetailRepository ordDetailRepository;

//    public List<OrdDetailVO> getAll(){
//        return ordDetailRepository.findAll();
//    }


}
