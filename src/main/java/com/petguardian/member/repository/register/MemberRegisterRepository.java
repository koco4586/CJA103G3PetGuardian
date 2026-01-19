package com.petguardian.member.repository.register;

import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRegisterRepository extends JpaRepository<Member,Integer> {
}
