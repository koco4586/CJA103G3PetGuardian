//package com.petguardian.admin.service;
//
//import com.petguardian.admin.dto.AdminRegisterDTO;
//import com.petguardian.admin.model.Admin;
//import com.petguardian.admin.repository.register.AdminRegisterRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AdminRegisterService {
//
//    @Autowired
//    private AdminRegisterRepository adminRegisterRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    public String register(AdminRegisterDTO adminRegisterDTO) {
//
//        String admName = adminRegisterDTO.getAdmName();
//        String admEmail = adminRegisterDTO.getAdmEmail();
//        String admAcc = adminRegisterDTO.getAdmAccount();
//        String admPw = adminRegisterDTO.getAdmPassword();
//        String admPwCheck = adminRegisterDTO.getAdmPwCheck();
//
//        if (!admPw.equals(admPwCheck)) {
//            return "密碼輸入不一致，請再次確認是否輸入正確。";
//        }
//
//        Admin admin = new Admin();
//        admin.setAdmName(admName);
//        admin.setAdmEmail(admEmail);
//        admin.setAdmAccount(admAcc);
//        admin.setAdmPassword(passwordEncoder.encode(admPw));
//
//        adminRegisterRepository.save(admin);
//
//        return "註冊成功";
//    }
//}
