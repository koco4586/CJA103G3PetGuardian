package com.petguardian.sitter.model;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for SitterMemberVO.
 * Provides read-only access to member data for sitter application module.
 * Decouples from MemberRegisterRepository.
 */
public interface SitterMemberRepository extends JpaRepository<SitterMemberVO, Integer> {
}
