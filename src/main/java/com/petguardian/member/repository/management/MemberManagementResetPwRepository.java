package com.petguardian.member.repository.management;

import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberManagementResetPwRepository extends JpaRepository<Member,Integer> {
}
