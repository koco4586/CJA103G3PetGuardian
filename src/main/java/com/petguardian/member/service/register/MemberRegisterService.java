package com.petguardian.member.service.register;

import com.petguardian.member.model.Member;
import com.petguardian.member.dto.MemberRegisterDTO;
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

        //確保預設值
        //會員性別2，會員狀態1，會員保姆權限0，會員商城總星星數0，會員商城總評價數0，會員登入失敗次數0
        Integer memSex = 2;
        Integer memStatus = 1;
        Integer memSitterStatus = 0;
        Integer memShopRatingScore = 0;
        Integer memShopRatingCount = 0;
        Integer memLoginAttempts = 0;
        
        if(!memPw .equals(memPwCheck)){
          return "密碼輸入不一致，請再次確認是否輸入正確。";
        }

        Member member = new Member();
        member.setMemName(memName);
        member.setMemEmail(memEmail);
        member.setMemAcc(memAcc);
        member.setMemPw(passwordEncoder.encode(memPw));
        //確保預設值
        member.setMemSex(memSex);
        member.setMemStatus(memStatus);
        member.setMemSitterStatus(memSitterStatus);
        member.setMemShopRatingScore(memShopRatingScore);
        member.setMemShopRatingCount(memShopRatingCount);
        member.setMemLoginAttempts(memLoginAttempts);
        

        memberRegisterRepository.save(member);

        return "註冊成功";
    }

}
