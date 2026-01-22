package com.petguardian.sitter.model;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 會員資料唯讀 Repository (Sitter模組專用)
 * 
 * 提供保姆模組所需的會員基本資料查詢 (唯讀)
 * 目的：解耦保姆申請與會員註冊模組，避免直接依賴 MemberRegisterRepository
 */
public interface SitterMemberRepository extends JpaRepository<SitterMemberVO, Integer> {
}
