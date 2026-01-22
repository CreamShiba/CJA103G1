package com.karshop.membertest.model;

import com.karshop.rating.model.RatingVO;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "member")
public class MemberVO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "mem_name")
    private String memName;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL ,fetch = FetchType.LAZY)
    private List<RatingVO> rating;


    // Getter / Setter
    public Integer getMemberNo() { return memberNo; }
    public void setMemberNo(Integer memberNo) { this.memberNo = memberNo; }
    public String getMemName() { return memName; }
    public void setMemName(String memName) { this.memName = memName; }

    public List<RatingVO> getRating() {
        return rating;
    }

    public void setRating(List<RatingVO> rating) {
        this.rating = rating;
    }
}