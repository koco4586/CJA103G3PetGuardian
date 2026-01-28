package com.petguardian.sitter.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 會員資料唯讀 Repository (Sitter模組專用)
 * 
 * 提供保姆模組所需的會員基本資料查詢 (唯讀)
 * 目的：解耦保姆申請與會員註冊模組，避免直接依賴 MemberRegisterRepository
 */
public interface SitterMemberRepository extends JpaRepository<SitterMemberVO, Integer> {
    @Modifying
    @Query(value = "UPDATE member SET mem_sitter_status = ?2 WHERE mem_id = ?1", nativeQuery = true)
    void updateMemSitterStatus(Integer memId, Integer status);
}
