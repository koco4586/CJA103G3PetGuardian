package com.petguardian.news.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsTypeRepository extends JpaRepository<NewsType, Integer> {
    // Spring Data JPA 會自動實作基本 CRUD
    // findAll(), findById(), save(), deleteById() 等方法
}