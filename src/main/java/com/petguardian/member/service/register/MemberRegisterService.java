package com.petguardian.member.service.register;

import com.petguardian.member.config.MemberRegisterConfig;
import com.petguardian.member.model.Member;
import com.petguardian.member.model.register.MemberRegisterDTO;
import com.petguardian.member.repository.register.MemberRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberRegisterService {

    @Autowired
    private MemberRegisterRepository memberRegisterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(MemberRegisterDTO memberRegisterDTO){

        String memName = memberRegisterDTO.getMemName();
        String memEmail = memberRegisterDTO.getMemEmail();
        String memAcc = memberRegisterDTO.getMemAcc();
        String memPw = memberRegisterDTO.getMemPw();
        String memPwCheck = memberRegisterDTO.getMemPwCheck();

        if(!memPw .equals(memPwCheck)){
          return "密碼輸入不一致，請再次確認是否輸入正確。";
        }

        Member member = new Member();
        member.setMemName(memName);
        member.setMemEmail(memEmail);
        member.setMemAcc(memAcc);
        member.setMemPw(passwordEncoder.encode(memPw));

        memberRegisterRepository.save(member);

        return "註冊成功";
    }

}
