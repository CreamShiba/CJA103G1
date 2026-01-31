package com.karshop.install.service;

import com.karshop.admins.model.AdminService;
import com.karshop.admins.model.AdminVO;
import com.karshop.members.model.MembersRepository;
import com.karshop.members.model.MembersVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MembersRepository membersRepository;
    private final AdminService adminService;

    /**
     * Mock login for member - direct database query
     */
    public MembersVO loginAsMember(String account, String password) {
        return membersRepository.findByMemAcc(account)
                .filter(member -> member.getMemPwd().equals(password))
                .orElse(null);
    }

    /**
     * Mock login for admin - direct database query
     */
    public AdminVO loginAsAdmin(String account, String password) {
        Optional<AdminVO> opt = adminService.findByAdminAcc(account);
        if (opt.isPresent()) {
            AdminVO admin = opt.get();
            if (admin.getAdminPwd().equals(password)) {
                return admin;
            }
        }
        return null;
    }
}
