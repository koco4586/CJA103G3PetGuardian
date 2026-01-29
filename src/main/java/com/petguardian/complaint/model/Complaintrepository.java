package com.petguardian.complaint.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Complaintrepository extends JpaRepository<ComplaintVO, Integer> {
    // 繼承後，基礎的 CRUD（save, findAll, delete...）就都自動有了

    /**
     * 計算指定檢舉狀態的檢舉數量
     */
    long countByReportStatus(Integer reportStatus);
}