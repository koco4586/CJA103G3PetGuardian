package com.petguardian.member.repository.adminmembermanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petguardian.member.model.Member;

public interface AdminMemberManagementUpdateStatusRepository extends JpaRepository<Member,Integer>{

}
