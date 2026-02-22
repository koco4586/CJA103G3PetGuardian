package com.petguardian.member.repository.management;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petguardian.member.model.Member;

public interface MemberManagementPreSearchRepository extends JpaRepository<Member, Integer> {

}
