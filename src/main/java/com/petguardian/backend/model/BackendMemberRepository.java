package com.petguardian.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// 假設您有一個基本的 Member Entity
import com.petguardian.member.model.Member;

@Repository
public interface BackendMemberRepository extends JpaRepository<Member, Integer> {

    /**
     * 計算所有啟用的會員
     * 直接使用 SQL 查詢資料庫 table: member
     * mem_status = 1 (啟用)
     */
    @Query(value = "SELECT count(*) FROM member WHERE mem_status = 1", nativeQuery = true)
    long countActiveMembers();
}