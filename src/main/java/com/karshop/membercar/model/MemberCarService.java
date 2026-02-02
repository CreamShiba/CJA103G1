package com.karshop.membercar.model;

import com.karshop.membercar.model.MemberCarVO;
import com.karshop.membercar.model.MemberCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberCarService {

  @Autowired
  private MemberCarRepository repository;

  // 查詢某會員的所有車輛
  public List<MemberCarVO> getCarsByMemberId(Integer memberId) {
    return repository.findByMember_MemNo(memberId);
  }

  // 新增車輛
  @Transactional
  public MemberCarVO addCar(MemberCarVO memberCar) {
    return repository.save(memberCar);
  }

  // 刪除車輛
  @Transactional
  public void deleteCar(Integer memberCarNo) {
    repository.deleteById(memberCarNo);
  }
}