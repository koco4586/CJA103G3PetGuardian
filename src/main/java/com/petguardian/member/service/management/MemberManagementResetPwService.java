package com.petguardian.member.service.management;

import com.petguardian.member.dto.MemberManagementResetPwDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementResetPwRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberManagementResetPwService {

    @Autowired
    private MemberManagementResetPwRepository memberManagementResetPwRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public String resetpw(MemberManagementResetPwDTO memberManagementResetPwDTO,Integer memId){

        String memPw = memberManagementResetPwDTO.getMemPw();

        String memPwCheck = memberManagementResetPwDTO.getMemPwCheck();

        if(!memPw.equals(memPwCheck)){
            return "密碼輸入不一致，請再次確認是否輸入正確。";
        }

        Member member = memberManagementResetPwRepository.findById(memId).orElse(null);

        member.setMemPw(passwordEncoder.encode(memPw));

        memberManagementResetPwRepository.save(member);

            return "密碼變更成功";
    }
}
