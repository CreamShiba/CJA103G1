package com.karshop.membercar.model;

import com.karshop.members.model.MembersVO;
import com.karshop.carcategory.model.CarCategoryVO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "member_car")
public class MemberCarVO implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_car_no")
  private Integer memberCarNo;

  // 雙向或單向關聯 Member
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_no", referencedColumnName = "member_no")
  private MembersVO member;

  // 關聯 CarCategory (車型)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "car_category_no", referencedColumnName = "car_category_no")
  private CarCategoryVO carCategory;

  public MemberCarVO() {}

  // Getters and Setters

  public Integer getMemberCarNo() {
    return memberCarNo;
  }

  public void setMemberCarNo(Integer memberCarNo) {
    this.memberCarNo = memberCarNo;
  }

  public MembersVO getMember() {
    return member;
  }

  public void setMember(MembersVO member) {
    this.member = member;
  }

  public CarCategoryVO getCarCategory() {
    return carCategory;
  }

  public void setCarCategory(CarCategoryVO carCategory) {
    this.carCategory = carCategory;
  }
}