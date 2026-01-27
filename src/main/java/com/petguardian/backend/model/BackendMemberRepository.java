package com.petguardian.backend.model;

import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 後台專用會員 Repository
 * 僅供後台統計功能使用
 */
@Repository
public interface BackendMemberRepository extends JpaRepository<Member, Integer> {

    /**
     * 計算指定會員狀態的會員數量
     * mem_status = 0 表示正常會員，1 表示停權
     */
    long countByMemStatus(Integer memStatus);
}