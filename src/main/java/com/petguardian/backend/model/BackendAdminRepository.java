package com.petguardian.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// 假設您有一個基本的 Admin Entity，如果沒有，請告訴我，我們改用 Object
import com.petguardian.admin.model.Admin;

@Repository
public interface BackendAdminRepository extends JpaRepository<Admin, Integer> {

    /**
     * 計算所有啟用的管理員
     * 直接使用 SQL 查詢資料庫 table: admin
     * adm_status = 1 (啟用)
     */
    @Query(value = "SELECT count(*) FROM admin WHERE adm_status = 1", nativeQuery = true)
    long countActiveAdmins();
}