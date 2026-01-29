package com.petguardian.complaint.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Complaintservice {
    @Autowired
    private Complaintrepository repository;

    public void insert(ComplaintVO vo) {
        repository.save(vo);
    }
    
    public List<ComplaintVO> getAll() {
        return repository.findAll(); // 調用 JpaRepository 內建的 findAll()
    }
    
    public ComplaintVO getOne(Integer id) {
        Optional<ComplaintVO> optional = repository.findById(id);
        return optional.orElse(null); // 如果找不到就回傳 null，或拋出例外
    }

    // 更新狀態
    public void updateStatus(Integer id, Integer newStatus) {
        ComplaintVO vo = getOne(id);
        if (vo != null) {
            vo.setReportStatus(newStatus);
            repository.save(vo); // JPA 會根據 ID 自動執行 Update
        }
    }
}