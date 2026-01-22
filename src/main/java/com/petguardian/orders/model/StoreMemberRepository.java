package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 會員資料 Repository（Read-only，供二手商城模組使用）
 */
@Repository
public interface StoreMemberRepository extends JpaRepository<StoreMemberVO, Integer> {
}
