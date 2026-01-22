package com.petguardian.member.service.login;

import com.petguardian.member.dto.MemberLoginDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.login.MemberLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberLoginService {


    @Autowired
    private MemberLoginRepository memberLoginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member login(MemberLoginDTO memberLoginDTO) {

        String userAccount = memberLoginDTO.getMemAcc();

        String userPassword = memberLoginDTO.getMemPw();

        Member member = memberLoginRepository.findByMemAcc(userAccount);

        if(member == null) {
            return null;
        }

        if( !passwordEncoder.matches(userPassword,member.getMemPw()) ) {
            return null;
        }

        return member;

    }

}
