package com.petguardian.admin.service.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.petguardian.admin.dto.login.AdminLoginDTO;
import com.petguardian.admin.repository.login.AdminLoginRepository;
import com.petguardian.admin.dto.login.AdminLoginDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.member.repository.login.MemberLoginRepository;

@Service
public class AdminLoginService {

	@Autowired
    private AdminLoginRepository adminLoginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Admin adminlogin(AdminLoginDTO adminLoginDTO) {

        String admAccount = adminLoginDTO.getAdmAccount();

        String admPassword = adminLoginDTO.getAdmPassword();

        Admin admin = adminLoginRepository.findByAdmAccount(admAccount);

        if(admin == null) {
            return null;
        }

        if( !passwordEncoder.matches(admPassword,admin.getAdmPassword()) ) {
            return null;
        }

        return admin;

    }

}
