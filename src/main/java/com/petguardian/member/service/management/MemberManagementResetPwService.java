package com.petguardian.member.service.management;

import com.petguardian.member.dto.MemberManagementResetPwDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementResetPwRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberManagementResetPwService {

    @Autowired
    private MemberManagementResetPwRepository memberManagementResetPwRepository;

    public String resetpw(MemberManagementResetPwDTO memberManagementResetPwDTO,Integer memId){

        String memPw = memberManagementResetPwDTO.getMemPw();

        String memPwCheck = memberManagementResetPwDTO.getMemPwCheck();

        if(!memPw.equals(memPwCheck)){
            return "密碼輸入不一致，請再次確認是否輸入正確。";
        }

        Member member = memberManagementResetPwRepository.findById(memId).orElse(null);

        member.setMemPw(memPw);

        memberManagementResetPwRepository.save(member);

            return "密碼變更成功";
    }
}
