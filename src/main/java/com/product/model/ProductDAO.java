package com.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDAO extends JpaRepository<ProductVO, Integer> {
    // 查詢特定賣家(會員)的商品 (給賣家後台用)
    List<ProductVO> findByMemId(Integer memId);
    
    // 查詢上架中的商品 (給前台商城用)
    List<ProductVO> findByProState(Integer proState);
}