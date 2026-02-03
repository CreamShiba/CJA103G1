package com.karshop.model.repository;

import com.karshop.members.model.MembersVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("forumMemberRepository")
public interface ForumMemberRepository extends JpaRepository<MembersVO, Integer> {

    // 🟢 這裡的 MembersVO 已經包含 memNo 屬性了
    MembersVO findByMemUsername(String memUsername);
}