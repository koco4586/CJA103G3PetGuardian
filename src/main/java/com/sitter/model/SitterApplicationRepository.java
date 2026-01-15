package com.sitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SitterApplicationRepository extends JpaRepository<SitterApplicationVO, Integer> {
	// 檢查是否已申請
	boolean existsByMember_MemId(Integer memId);

	// 查會員自己的申請
	SitterApplicationVO findByMember_MemId(Integer memId);

	// 管理員：查待審核
	List<SitterApplicationVO> findByAppStatusOrderByAppCreatedAtDesc(Byte appStatus);
}
