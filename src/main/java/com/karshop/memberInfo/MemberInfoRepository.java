package com.karshop.memberInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, Integer> {
    @Query("SELECT m FROM MemberInfo m WHERE " +
            "(:memberNo IS NULL OR m.memberNo = :memberNo) AND " +
            "(:kw IS NULL OR (m.memberAccount LIKE %:kw% OR m.memberName LIKE %:kw% OR m.memberEmail LIKE %:kw%)) AND " +
            "(:status IS NULL OR m.accountStatus = :status) AND " +
            "(:isSeller IS NULL OR m.sellerStatus = :isSeller) AND " +
            "(:isEngineer IS NULL OR m.engineerStatus = :isEngineer)")

    List<MemberInfo> findByCompositeQuery(
            @Param("memberNo") Integer memberNo,
            @Param("kw") String kw,
            @Param("status") Integer status,
            @Param("isSeller") Integer isSeller,
            @Param("isEngineer") Integer isEngineer);
}