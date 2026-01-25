package com.petguardian.member.repository.management;

import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberManagementDeactivateRepository extends JpaRepository<Member,Integer> {

}
