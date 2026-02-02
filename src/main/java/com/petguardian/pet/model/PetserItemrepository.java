package com.petguardian.pet.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetserItemrepository extends JpaRepository<PetServiceItem, Integer> {

    // 1. 顯示所有上架服務 (用於初始頁面列出所有選項)
//    List<PetServiceItem> findByServiceStatus(Integer status);

    // 2. 根據關鍵字搜尋服務 (搜尋框功能)
    List<PetServiceItem> findByServiceTypeContaining(String keyword);

    // 3. 根據服務類型尋找 (如果你有分類，例如：散步類、洗澡類)
    // List<PetServiceItem> findByServiceCategory(String category);

}