package com.petguardian.member.repository.login;

import com.petguardian.member.dto.MemberLoginDTO;
import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLoginRepository  extends JpaRepository<Member,Integer>{

    public Member findByMemAcc(String memAcc);

}
