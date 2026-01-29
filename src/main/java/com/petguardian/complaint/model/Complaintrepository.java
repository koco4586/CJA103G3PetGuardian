package com.petguardian.complaint.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Complaintrepository extends JpaRepository<ComplaintVO, Integer> {
    // 繼承後，基礎的 CRUD（save, findAll, delete...）就都自動有了
}